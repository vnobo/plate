package com.platform.boot.security.captcha;

import lombok.Data;

import java.io.Serializable;

/**
 * captcha token entity class
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data(staticConstructor = "of")
public class CaptchaToken implements Serializable {
    /**
     * http captcha header name
     */
    private final String headerName;

    /**
     * http captcha parameter name
     */
    private final String parameterName;

    /**
     * captcha
     */
    private final String captcha;
}