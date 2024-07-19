package com.plate.authorization.security;

import com.plate.authorization.security.core.AuthenticationToken;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    @GetMapping("token")
    public AuthenticationToken token(HttpSession session, Authentication authentication) {
        return AuthenticationToken.build(session, authentication);
    }

}