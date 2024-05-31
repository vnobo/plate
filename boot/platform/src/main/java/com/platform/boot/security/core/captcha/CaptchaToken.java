package com.platform.boot.security.core.captcha;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @param headerName    http captcha header name
 * @param parameterName http captcha parameter name
 * @param captcha       captcha
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record CaptchaToken(String headerName, String parameterName, String captcha) implements Serializable {
    public static CaptchaToken of(String headerName, String parameterName, String captcha) {
        return new CaptchaToken(headerName, parameterName, captcha);
    }

    public Boolean validate(String code) {
        Assert.notNull(code, "captcha code must not be null");
        return this.captcha.equalsIgnoreCase(code);
    }
}