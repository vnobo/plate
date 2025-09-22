package com.plate.boot.security.captcha;

import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Captcha token record that holds captcha information including header name, parameter name and captcha value.
 * This record is used to store and validate captcha tokens in web applications.
 *
 * @param headerName    HTTP captcha header name
 * @param parameterName HTTP captcha parameter name
 * @param captcha       Captcha value
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record CaptchaToken(String headerName, String parameterName, String captcha) implements Serializable {
    /**
     * Static factory method to create a new CaptchaToken instance
     *
     * @param headerName    HTTP header name for captcha
     * @param parameterName HTTP parameter name for captcha
     * @param captcha       Captcha value
     * @return New CaptchaToken instance
     */
    public static CaptchaToken of(String headerName, String parameterName, String captcha) {
        return new CaptchaToken(headerName, parameterName, captcha);
    }

    /**
     * Validate the provided captcha code against the stored captcha value
     *
     * @param code Captcha code to validate
     * @return true if the provided code matches the stored captcha (case-insensitive), false otherwise
     * @throws IllegalArgumentException if the provided code is null
     */
    public Boolean validate(String code) {
        Assert.notNull(code, "captcha code must not be null");
        return this.captcha.equalsIgnoreCase(code);
    }
}