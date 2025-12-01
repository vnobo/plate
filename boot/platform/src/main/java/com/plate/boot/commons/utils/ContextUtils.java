package com.plate.boot.commons.utils;

import com.github.f4b6a3.uuid.UuidCreator;
import com.plate.boot.commons.base.AbstractEvent;
import com.plate.boot.security.SecurityDetails;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class providing context-related operations and services for the application.
 * This class is designed as a Spring Component and implements InitializingBean to ensure
 * proper setup during application initialization.
 * <p>
 * It offers functionalities such as encoding strings to MD5, retrieving client IP addresses,
 * handling security details within reactive context, serializing UserAuditor objects,
 * generating unique IDs, and initializing utility setup with logging.
 */
@Log4j2
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ContextUtils implements InitializingBean {

    public final static UUID DEFAULT_UUID_CODE = UUID.fromString("00000000-0000-0000-0000-000000000000");
    /**
     * Constant defining the role identifier for administrators within the system.
     * This role grants access to administrative functionalities and permissions.
     */
    public final static String RULE_ADMINISTRATORS = "ROLE_SYSTEM_ADMINISTRATORS";
    /**
     * Constants for the context key used to store CSRF token information.
     * This string represents the identifier used to retrieve or store CSRF tokens in a context such as a thread local,
     * request attribute, or any other data structure toSql keys are strings.
     */
    public final static String CSRF_TOKEN_CONTEXT = "CSRF_TOKEN_CONTEXT";

    /**
     * An array of strings representing the possible header names that could contain
     * the client's IP address in HTTP requests. This is typically used when working
     * behind proxies or load balancers toSql the original client IP is not directly
     * available in the standard {@code REMOTE_ADDR} header.
     * <p>
     * The list includes common headers like {@code X-Forwarded-For}, {@code X-Real-IP},
     * and others that are often set by proxies to facilitate correct identification
     * of the originating IP address of the client.
     */
    private final static String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    /**
     * A shared static instance of Jackson's {@link JsonMapper}, which is utilized for
     * serializing and deserializing Java objects to and from JSON format. This singleton pattern
     * helps in ensuring consistent JSON processing across the application and reduces the
     * overhead of creating multiple ObjectMapper instances.
     *
     * <p>Usage of this mapper should be considered for general-purpose JSON operations within the application,
     * unless specific configurations are required for certain use cases, in which case a separate
     * ObjectMapper instance with tailored configurations should be created.
     *
     * <p>It is important to note that since this is a static shared resource, any configuration changes
     * made to this instance will affect all parts of the application using it. Therefore, caution must
     * be exercised when modifying its settings.
     */
    public static JsonMapper OBJECT_MAPPER;
    /**
     * Global cache manager instance
     * <p>
     * This static member variable provides an application-level cache management access point, making it convenient to operate the cache across different layers.
     * Ensure that the injection or initialization of this instance is completed during the system initialization phase (typically through a dependency injection framework like Spring configuration).
     * <p>
     * Notes:
     * 1. Since it uses a public static variable, ensure that the CacheManager implementation itself is thread-safe when used in a multi-threaded environment.
     * 2. Avoid directly modifying this variable in unit tests or uncontrolled environments.
     * 3. It is recommended to obtain the cache manager instance through dependency injection, and use this static variable only in special scenarios.
     */
    public static CacheManager CACHE_MANAGER;

    /**
     * Global application event publisher instance
     * <p>
     * This static member variable provides an application-level event publishing access point, making it convenient to publish events across different layers.
     * Ensure that the injection or initialization of this instance is completed during the system initialization phase (typically through a dependency injection framework like Spring configuration).
     * <p>
     * Notes:
     * 1. Since it uses a public static variable, ensure that the ApplicationEventPublisher implementation itself is thread-safe when used in a multi-threaded environment.
     * 2. Avoid directly modifying this variable in unit tests or uncontrolled environments.
     */
    public static ApplicationEventPublisher APPLICATION_EVENT_PUBLISHER;

    private final Dependencies dependencies;

    /**
     * Initializes the ContextUtils class with necessary dependencies.
     *
     * @param objectMapper The ObjectMapper instance used for JSON serialization and deserialization.
     * @param cacheManager The CacheManager instance used for cache operations.
     * @param publisher    The ApplicationEventPublisher instance used for event publishing.
     */
    ContextUtils(JsonMapper objectMapper, CacheManager cacheManager,
                 ApplicationEventPublisher publisher) {
        this.dependencies = new Dependencies(objectMapper, cacheManager, publisher);
    }

    /**
     * Publishes an event to the application event publisher.
     *
     * @param object The event object to be published.
     */
    public static void eventPublisher(AbstractEvent<?> object) {
        log.debug("Publishing event: {}", object);
        ContextUtils.APPLICATION_EVENT_PUBLISHER.publishEvent(object);
    }

    /**
     * Creates a {@link DelegatingPasswordEncoder} with default mappings. Additional
     * mappings may be added and the encoding will be updated to conform with best
     * practices. However, due to the nature of {@link DelegatingPasswordEncoder} the
     * updates should not impact users. The mappings current are:
     *
     * <ul>
     * <li>bcrypt - {@link BCryptPasswordEncoder} (Also used for encoding)</li>
     * <li>ldap -
     * {@link org.springframework.security.crypto.password.LdapShaPasswordEncoder}</li>
     * <li>MD4 -
     * {@link org.springframework.security.crypto.password.Md4PasswordEncoder}</li>
     * <li>MD5 - {@code new MessageDigestPasswordEncoder("MD5")}</li>
     * <li>noop -
     * {@link org.springframework.security.crypto.password.NoOpPasswordEncoder}</li>
     * <li>pbkdf2 - {@link Pbkdf2PasswordEncoder#defaultsForSpringSecurity_v5_5()}</li>
     * <li>pbkdf2@SpringSecurity_v5_8 -
     * {@link Pbkdf2PasswordEncoder#defaultsForSpringSecurity_v5_8()}</li>
     * <li>scrypt - {@link SCryptPasswordEncoder#defaultsForSpringSecurity_v4_1()}</li>
     * <li>scrypt@SpringSecurity_v5_8 -
     * {@link SCryptPasswordEncoder#defaultsForSpringSecurity_v5_8()}</li>
     * <li>SHA-1 - {@code new MessageDigestPasswordEncoder("SHA-1")}</li>
     * <li>SHA-256 - {@code new MessageDigestPasswordEncoder("SHA-256")}</li>
     * <li>sha256 -
     * {@link org.springframework.security.crypto.password.StandardPasswordEncoder}</li>
     * <li>argon2 - {@link Argon2PasswordEncoder#defaultsForSpringSecurity_v5_2()}</li>
     * <li>argon2@SpringSecurity_v5_8 -
     * {@link Argon2PasswordEncoder#defaultsForSpringSecurity_v5_8()}</li>
     * </ul>
     *
     * @param encodingId The encoding ID to use as the default encoder
     * @return the {@link PasswordEncoder} to use
     */
    @SuppressWarnings("deprecation")
    public static PasswordEncoder createDelegatingPasswordEncoder(String encodingId) {
        if (!StringUtils.hasLength(encodingId)) {
            encodingId = "bcrypt";
        }
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        // 推荐的密码编码器
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2@SpringSecurity_v5_8", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("scrypt@SpringSecurity_v5_8", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("argon2@SpringSecurity_v5_8", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());

        // 向后兼容的密码编码器，但不推荐用于新密码
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_5());
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v4_1());
        encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_2());

        // 保留但标记为过时的密码编码器，仅用于验证旧密码
        @Deprecated(since = "5.7", forRemoval = true)
        PasswordEncoder ldapEncoder = new org.springframework.security.crypto.password.LdapShaPasswordEncoder();
        encoders.put("ldap", ldapEncoder);

        @Deprecated(since = "5.7", forRemoval = true)
        PasswordEncoder md4Encoder = new org.springframework.security.crypto.password.Md4PasswordEncoder();
        encoders.put("MD4", md4Encoder);

        @Deprecated(since = "5.7", forRemoval = true)
        PasswordEncoder md5Encoder = new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5");
        encoders.put("MD5", md5Encoder);

        @Deprecated(since = "5.7", forRemoval = true)
        PasswordEncoder sha1Encoder = new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1");
        encoders.put("SHA-1", sha1Encoder);

        @Deprecated(since = "5.7", forRemoval = true)
        PasswordEncoder sha256Encoder = new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256");
        encoders.put("SHA-256", sha256Encoder);

        @Deprecated(since = "5.7", forRemoval = true)
        PasswordEncoder standardEncoder = new org.springframework.security.crypto.password.StandardPasswordEncoder();
        encoders.put("sha256", standardEncoder);

        @Deprecated(since = "5.7", forRemoval = true)
        PasswordEncoder noOpEncoder = org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
        encoders.put("noop", noOpEncoder);

        return new DelegatingPasswordEncoder(encodingId, encoders);
    }

    /**
     * Encodes the given input string into its SHA-256 hash representation, which is then
     * base64 encoded for a more compact and URL-friendly format.
     *
     * @param input The string to be encoded into SHA-256 hash.
     * @return A base64 encoded string representing the SHA-256 hash of the input string.
     */
    public static String encodeToSHA256(String input) {
        if (input == null) {
            return null;
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return null;
        }
    }

    /**
     * Retrieves the client's IP address from the given server HTTP request.
     * This method iterates over a set of common headers used to carry the client's IP address
     * and returns the first valid, non-private IP address found. For X-Forwarded-For header,
     * it extracts the first IP from the comma-separated list. If none of these headers yield a valid IP,
     * it falls back to extracting the IP from the remote address of the request.
     *
     * @param httpRequest The server HTTP request from which to extract the client's IP address.
     * @return The client's IP address as a string, or the remote address's host address if no
     * explicit IP is found in the headers.
     */
    public static String getClientIpAddress(ServerHttpRequest httpRequest) {
        if (httpRequest == null) {
            return null;
        }

        HttpHeaders headers = httpRequest.getHeaders();
        for (String header : IP_HEADER_CANDIDATES) {
            List<String> ipList = headers.get(header);
            if (ipList != null && !ipList.isEmpty()) {
                String ip = ipList.getFirst();
                // 处理X-Forwarded-For可能包含多个IP地址的情况
                if ("X-Forwarded-For".equalsIgnoreCase(header) && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                // 验证IP地址是否有效且不是私有地址
                if (isValidPublicIp(ip)) {
                    return ip;
                }
            }
        }
        // 回退到远程地址
        if (httpRequest.getRemoteAddress() != null && httpRequest.getRemoteAddress().getAddress() != null) {
            return httpRequest.getRemoteAddress().getAddress().getHostAddress();
        }
        return null;
    }

    /**
     * 检查IP地址是否有效且不是私有地址
     */
    private static boolean isValidPublicIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }

        try {
            java.net.InetAddress address = java.net.InetAddress.getByName(ip);
            // 检查是否是私有地址、本地地址或回环地址
            return !(address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                    || "0.0.0.0".equals(ip) || "::".equals(ip) || ip.startsWith("127.")
                    || ip.startsWith("10.") || ip.startsWith("172.16.") || ip.startsWith("192.168."));
        } catch (java.net.UnknownHostException e) {
            return false;
        }
    }

    /**
     * Retrieves the security details of the currently authenticated user.
     * <p>
     * This method accesses the security context asynchronously and extracts the principal,
     * which is then cast to a {@link SecurityDetails} object. It is designed to be used
     * within a reactive environment toSql security information is required for further processing.
     * </p>
     *
     * @return A {@link Mono} emitting the {@link SecurityDetails} associated with the
     * current authentication context, or an empty Mono if no authentication is present.
     */
    public static Mono<SecurityDetails> securityDetails() {
        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
            Authentication authentication = securityContext.getAuthentication();
            if (authentication == null) {
                return Mono.empty();
            }
            Object principal = authentication.getPrincipal();
            return principal instanceof SecurityDetails ? Mono.just((SecurityDetails) principal) : Mono.empty();
        });
    }

    /**
     * Generates a new time-ordered UUID (Universally Unique Identifier) as the next identifier.
     * This method uses UuidCreator.getTimeOrderedEpoch() which generates time-based UUIDs
     * that are sequentially ordered and suitable for database primary keys.
     *
     * @return A newly created {@link UUID} instance, providing a unique and time-ordered identifier.
     */
    public static UUID nextId() {
        return UuidCreator.getTimeOrderedEpoch();
    }

    /**
     * Overrides the {@link InitializingBean#afterPropertiesSet()} method.
     * This method is called by the containing BeanFactory after it has set all bean properties.
     * It is typically used to perform initialization that requires all properties to be set.
     * <p>
     * Logs a message indicating the initialization of the [ContextUtils] component.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Initializing utils [ContextUtils]...");
        OBJECT_MAPPER = this.dependencies.objectMapper;
        CACHE_MANAGER = this.dependencies.cacheManager;
        APPLICATION_EVENT_PUBLISHER = this.dependencies.eventPublisher;
    }

    private record Dependencies(JsonMapper objectMapper, CacheManager cacheManager,
                                ApplicationEventPublisher eventPublisher) {
    }
}