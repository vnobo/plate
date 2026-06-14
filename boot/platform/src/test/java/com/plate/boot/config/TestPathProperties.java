package com.plate.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for integration test endpoint paths.
 * <p>
 * Binds properties prefixed with {@code test.paths} from the test application.yml,
 * providing a single source of truth for API paths used in integration tests.
 * Path values are derived from {@code spring.webflux.apiversion.default} combined
 * with {@code spring.webflux.properties.path-prefixes}, ensuring consistency with
 * the actual runtime path configuration.
 * </p>
 * <p>
 * Note: This class does not use Lombok annotations because Lombok is configured
 * as {@code compileOnly} and is not available on the test classpath.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@ConfigurationProperties(prefix = "test.paths")
public class TestPathProperties {

    /** Security API base path prefix, e.g. "/sec/v1". */
    private String secPrefix = "/sec/v1";

    /** OAuth2 endpoint base path, e.g. "/sec/v1/oauth2". */
    private String oauth2Base = "/sec/v1/oauth2";

    /** Captcha endpoint base path, e.g. "/sec/v1/captcha". */
    private String captchaBase = "/sec/v1/captcha";

    /** Relational API base path prefix, e.g. "/rel/v1". */
    private String relPrefix = "/rel/v1";

    public String getSecPrefix() {
        return secPrefix;
    }

    public void setSecPrefix(String secPrefix) {
        this.secPrefix = secPrefix;
    }

    public String getOauth2Base() {
        return oauth2Base;
    }

    public void setOauth2Base(String oauth2Base) {
        this.oauth2Base = oauth2Base;
    }

    public String getCaptchaBase() {
        return captchaBase;
    }

    public void setCaptchaBase(String captchaBase) {
        this.captchaBase = captchaBase;
    }

    public String getRelPrefix() {
        return relPrefix;
    }

    public void setRelPrefix(String relPrefix) {
        this.relPrefix = relPrefix;
    }
}
