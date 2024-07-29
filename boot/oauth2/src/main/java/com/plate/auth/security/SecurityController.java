package com.plate.auth.security;

import com.plate.auth.security.core.AuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("oauth2")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @GetMapping("token")
    public AuthenticationToken token(HttpSession session, Authentication authentication,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        this.securityContextRepository.saveContext(securityContext, request, response);
        return AuthenticationToken.build(session, authentication);
    }

}