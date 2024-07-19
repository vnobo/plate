package com.plate.auth.config;

import com.plate.auth.commons.ErrorResponse;
import com.plate.auth.commons.utils.ContextUtils;
import com.plate.auth.security.core.UserAuditor;
import com.plate.auth.security.core.UserAuditorAware;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;
import java.util.List;

import static com.plate.auth.config.SessionConfig.XML_HTTP_REQUEST;
import static com.plate.auth.config.SessionConfig.X_REQUESTED_WITH;
import static org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive.COOKIES;


/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class SecurityConfig {

    @Bean
    public AuditorAware<UserAuditor> auditorProvider() {
        return new UserAuditorAware();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain springSecurity(HttpSecurity http) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName("continue");
        http.authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .anyRequest().authenticated())
                .securityContext((securityContext) -> securityContext.requireExplicitSave(true))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .maximumSessions(1))
                .httpBasic(Customizer.withDefaults())
                .formLogin((formLogin) -> formLogin.loginPage("/login").permitAll())
                .csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .logout((logout) -> logout.logoutUrl("/oauth2/logout")
                        .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(COOKIES))));
        return http.build();
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