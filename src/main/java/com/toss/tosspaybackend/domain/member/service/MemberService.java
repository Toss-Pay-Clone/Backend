package com.toss.tosspaybackend.domain.member.service;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import com.toss.tosspaybackend.config.security.jwt.JwtProvider;
import com.toss.tosspaybackend.config.security.jwt.JwtToken;
import com.toss.tosspaybackend.domain.member.dto.*;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.domain.member.service.validate.MemberValidate;
import com.toss.tosspaybackend.global.Response;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.util.redis.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.toss.tosspaybackend.global.exception.GlobalException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberValidate memberValidate;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TextEncryptor textEncryptor;
    private final SecurityProperties securityProperties;
    private final RedisUtils redisUtils;

    @Transactional
    public Response<RegisterResponse> register(RegisterRequest request) {

        memberValidate.validatePhoneNumber(request.phone());
        memberValidate.validateRRN(request.residentRegistrationNumberFront(), request.residentRegistrationNumberBack());
        memberValidate.validateGender(request.gender(), request.residentRegistrationNumberBack());
        memberValidate.validateBirthdate(request.birthdate(), request.residentRegistrationNumberFront(),
                request.residentRegistrationNumberBack(), request.gender());
        memberValidate.validateDuplicate(request.name(), request.phone(), request.residentRegistrationNumberFront());
        memberValidate.validateRegisterPassword(request.password(), request.phone(), request.birthdate());

        Member savedMember = memberRepository.save(request.toEntity(passwordEncoder));

        RegisterResponse responseData = RegisterResponse.builder()
                .id(savedMember.getId())
                .name(savedMember.getName())
                .createdAt(savedMember.getBaseTime().getCreatedAt())
                .build();

        return Response.<RegisterResponse>builder()
                .httpStatus(HttpStatus.CREATED.value())
                .message("회원가입에 성공했습니다.")
                .data(responseData)
                .build();
    }

    @Transactional(readOnly = true)
    public Response<JwtToken> login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Cookie[] cookies = httpRequest.getCookies();
        memberValidate.loginCookieExistsValidate(cookies);

        String encryptToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(securityProperties.getTokenHeader()))
                .findFirst().get()
                .getValue();

        String tokenCount = redisUtils.getData(encryptToken);
        // 시도 횟수가 5회 이상일 경우 계정 임시 차단 (여기서는 Manual로 진행함)(전역 Security Filter 등록 예정)
        memberValidate.validateEncryptToken(encryptToken);
        // tokenCount가 0인 경우 이후 Logic을 좀더 빠르게 수행하기 위해 password Caching
        if (tokenCount.equals("0")) {
            String decryptedPhone = textEncryptor.decrypt(encryptToken);
            Member member = memberRepository.findByPhone(decryptedPhone)
                    .orElseThrow(() -> new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, "해당 전화번호로 가입된 계정이 없습니다."));

            // 일치하지 않을 경우에만 Caching
            try {
                memberValidate.checkPassword(encryptToken, request.password(), member.getPassword());
            } catch (GlobalException ge) {
                redisUtils.setData(encryptToken + securityProperties.getPreLoginPasswordSuffix(), member.getPassword(), securityProperties.getPreLoginValidationMillisecond());
                throw ge;
            }
        }

        if (Integer.parseInt(tokenCount) >= 1) {
            // Caching된 Password를 이용하여 검증
            String cachedPassword = redisUtils.getData(encryptToken + securityProperties.getPreLoginPasswordSuffix());
            memberValidate.checkPassword(encryptToken, request.password(), cachedPassword);
        }

        Member member = memberRepository.findByPhone(textEncryptor.decrypt(encryptToken)).get();
        JwtToken jwtToken = jwtProvider.createJWTTokens(member);

        expirePreLoginToken(textEncryptor.decrypt(encryptToken));
        deletePreLoginCookie(httpResponse);
        createLoginCookie(jwtToken, httpResponse);

        return Response.<JwtToken>builder()
                .httpStatus(HttpStatus.OK.value())
                .message("로그인에 성공했습니다.")
                .data(jwtToken)
                .build();
    }

    @Transactional(readOnly = true)
    public Response<String> existenceCheck(ExistenceCheckRequest request, HttpServletResponse response) {

        boolean isExistsByPhone = memberRepository.existsByPhone(request.phone());
        if (!isExistsByPhone) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "해당 전화번호로 가입된 계정이 없습니다.");
        }

        memberValidate.raceConditionAttackCheck(request.phone());

        String encryptedToken = textEncryptor.encrypt(request.phone());
        Cookie tokenCookie = new Cookie(securityProperties.getTokenHeader(), encryptedToken);
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        response.addCookie(tokenCookie);

        redisUtils.setData(request.phone(), encryptedToken, securityProperties.getPreLoginValidationMillisecond());
        redisUtils.setData(encryptedToken, "0", securityProperties.getPreLoginValidationMillisecond());

        return Response.<String>builder()
                .httpStatus(HttpStatus.CREATED.value())
                .message("전화번호 확인이 완료되었습니다.")
                .data("비밀번호 인증을 진행해주세요.")
                .build();
    }

    private void createLoginCookie(JwtToken jwtToken, HttpServletResponse response) {
        Cookie accessToken = new Cookie(securityProperties.getAccessHeader(), jwtToken.accessToken());
        Cookie refreshToken = new Cookie(securityProperties.getRefreshHeader(), jwtToken.refreshToken());

        accessToken.setMaxAge(securityProperties.getAccessTokenValidationMillisecond());
        refreshToken.setMaxAge(securityProperties.getRefreshTokenValidationMillisecond());

        accessToken.setPath("/");
        refreshToken.setPath("/");
        accessToken.setHttpOnly(true);
        refreshToken.setHttpOnly(true);

        response.addCookie(accessToken);
        response.addCookie(refreshToken);
    }

    private void expirePreLoginToken(String phone) {
        String preLoginToken = redisUtils.getData(phone);
        String preLoginCount = redisUtils.getData(preLoginToken);

        redisUtils.deleteData(preLoginToken + securityProperties.getPreLoginPasswordSuffix());
        redisUtils.deleteData(preLoginCount);
        redisUtils.deleteData(preLoginToken);
        redisUtils.deleteData(phone);
    }

    private void deletePreLoginCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("encrypt_token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
