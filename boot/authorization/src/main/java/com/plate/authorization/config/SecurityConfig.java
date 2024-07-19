package com.plate.authorization.config;

import com.plate.authorization.commons.ErrorResponse;
import com.plate.authorization.commons.utils.ContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

import static com.plate.authorization.config.SessionConfig.XML_HTTP_REQUEST;
import static com.plate.authorization.config.SessionConfig.X_REQUESTED_WITH;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated());
        http.httpBasic(httpBasicSpec -> httpBasicSpec.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        http.formLogin(Customizer.withDefaults());
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        http.logout(logout -> logout.logoutUrl("/oauth2/logout").deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("*");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Log4j2
    static class CustomAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
        public CustomAuthenticationEntryPoint() {
            this.setRealmName("Plate Realm");
        }

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException e) throws IOException {
            String requestedWith = request.getHeader(X_REQUESTED_WITH);
            log.error("认证失败! 信息: {}", e.getMessage());
            if (isXmlHttpRequest(requestedWith)) {
                handleXmlHttpRequestFailure(request, response, e);
            } else {
                super.commence(request, response, e);
            }
        }

        private boolean isXmlHttpRequest(String requestedWith) {
            return requestedWith != null && requestedWith.contains(XML_HTTP_REQUEST);
        }

        private void handleXmlHttpRequestFailure(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationException e) throws IOException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            ErrorResponse errorResponse = createErrorResponse(request, e);
            response.sendError(401, ContextUtils.objectToString(errorResponse));
        }

        private ErrorResponse createErrorResponse(HttpServletRequest request, AuthenticationException e) {
            return ErrorResponse.of(request.getRequestId(), request.getServletPath(),
                    401, "认证失败,检查你的用户名,密码是否正确或安全密钥是否过期!", List.of(e.getMessage()));
        }

    }
}