package com.toss.tosspaybackend.config.security;

import com.toss.tosspaybackend.config.security.jwt.JwtAuthenticationFilter;
import com.toss.tosspaybackend.global.exception.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.Arrays;

@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final SecurityProperties securityProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AccountStatusFilter accountStatusFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(
                        exception -> exception.accessDeniedHandler(customAccessDeniedHandler))
                .sessionManagement(setSessionManagementConfig())
                .addFilterBefore(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(accountStatusFilter, JwtAuthenticationFilter.class)
                .authorizeHttpRequests(request ->
                        request.requestMatchers(securityProperties.getAuthWhitelist()).permitAll()
                                .anyRequest().authenticated())
//                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.text(securityProperties.getEncryptSecretKey(), securityProperties.getEncryptSecretSalt());
    }

    private Customizer<SessionManagementConfigurer<HttpSecurity>> setSessionManagementConfig() {
        return s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
