package com.toss.tosspaybackend.domain.member.service;

import com.toss.tosspaybackend.config.security.SecurityProperties;
import com.toss.tosspaybackend.config.security.jwt.JwtProvider;
import com.toss.tosspaybackend.config.security.jwt.JwtToken;
import com.toss.tosspaybackend.domain.member.dto.*;
import com.toss.tosspaybackend.domain.member.entity.Member;
import com.toss.tosspaybackend.domain.member.enums.AccountStatus;
import com.toss.tosspaybackend.domain.member.repository.MemberRepository;
import com.toss.tosspaybackend.domain.member.service.validate.MemberValidate;
import com.toss.tosspaybackend.global.Response;
import com.toss.tosspaybackend.global.exception.ErrorCode;
import com.toss.tosspaybackend.util.redis.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public Response<String> login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Cookie[] cookies = httpRequest.getCookies();
        memberValidate.loginCookieExistsValidate(cookies);

        String encryptToken = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(securityProperties.getTokenHeader()))
                .findFirst().get()
                .getValue();

        String decryptedPhone = textEncryptor.decrypt(encryptToken);
        // 계정 정지 확인
        memberValidate.accountStatusValidate(decryptedPhone);

        String tokenCount = redisUtils.getData(encryptToken);
        // 시도 횟수가 5회 이상일 경우 계정 임시 차단 (여기서는 Manual로 진행함)(전역 Security Filter 등록 예정)
        // tokenCount가 0인 경우 이후 Logic을 좀더 빠르게 수행하기 위해 password Caching
        if (tokenCount.equals("0")) {
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

        // Login 시도 1회 이상
        if (!tokenCount.equals("false") && Integer.parseInt(tokenCount) >= 1) {
            // Caching된 Password를 이용하여 검증
            String cachedPassword = redisUtils.getData(encryptToken + securityProperties.getPreLoginPasswordSuffix());
            memberValidate.checkPassword(encryptToken, request.password(), cachedPassword);
        }

        // 첫 로그인 시도
        Member member = memberRepository.findByPhone(textEncryptor.decrypt(encryptToken)).get();
        JwtToken jwtToken = jwtProvider.createJWTTokens(member);

        expirePreLoginToken(textEncryptor.decrypt(encryptToken));
        deletePreLoginCookie(httpResponse);
        createLoginCookie(jwtToken, httpResponse);

        return Response.<String>builder()
                .httpStatus(HttpStatus.OK.value())
                .message("로그인에 성공했습니다.")
                .data("시스템에 접속 가능한 상태입니다.")
                .build();
    }

    public Response<String> existenceCheck(ExistenceCheckRequest request, HttpServletResponse response) {

        boolean isExistsByPhone = memberRepository.existsByPhone(request.phone());
        if (!isExistsByPhone) {
            throw new GlobalException(ErrorCode.NOT_FOUND, "해당 전화번호로 가입된 계정이 없습니다.");
        }

        memberValidate.accountStatusValidate(request.phone());
        memberValidate.raceConditionAttackCheck(request.phone());

//        memberValidate.accountStatusValidate(request.phone());
        String encryptedToken = textEncryptor.encrypt(request.phone());
        Cookie tokenCookie = new Cookie(securityProperties.getTokenHeader(), encryptedToken);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(securityProperties.getEncryptTokenValidationSecond());
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

    public Response<String> passwordCheck(PasswordCheckRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        Member member = (Member) context.getAuthentication().getPrincipal();
        String redisCountDataKey = member.getPhone() + securityProperties.getPasswordCertificationSuffix();

        if (passwordEncoder.matches(request.password(), member.getPassword())) {
            redisUtils.deleteData(redisCountDataKey);
            return Response.<String>builder()
                    .httpStatus(HttpStatus.OK.value())
                    .message("비밀번호 인증에 성공하였습니다.")
                    .data("다음 단계를 진행해주세요.")
                    .build();
        }

        String certCount = redisUtils.getData(redisCountDataKey);
        if (!redisUtils.isExists(certCount)) {
            redisUtils.setData(redisCountDataKey, "1", securityProperties.getPasswordCertificationMillisecond());
            throw new GlobalException(ErrorCode.UNAUTHORIZED_REQUEST, "비밀번호가 일치하지 않습니다. 현재 시도 횟수: 1/5 회");
        } else {
            int count = Integer.parseInt(certCount);
            int tryCount = count + 1;
            if (tryCount >= 5) {
                redisUtils.deleteData(redisCountDataKey);
                member.setAccountStatus(AccountStatus.SUSPENDED);
                memberRepository.save(member);
                throw new GlobalException(ErrorCode.UNAUTHORIZED_REQUEST, "로그인 시도 횟수 초과로 인해 계정이 일시적으로 정지되었습니다.");
            }
            redisUtils.setData(redisCountDataKey, String.valueOf(tryCount), securityProperties.getPasswordCertificationMillisecond());
            throw new GlobalException(ErrorCode.UNAUTHORIZED_REQUEST, "비밀번호가 일치하지 않습니다. 현재 시도 횟수: " + tryCount + "/5 회");
        }
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
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
