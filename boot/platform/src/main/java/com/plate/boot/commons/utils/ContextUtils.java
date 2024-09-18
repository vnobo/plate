package com.plate.boot.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.core.UserAuditor;
import com.plate.boot.security.core.user.UsersService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

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
public final class ContextUtils implements InitializingBean {

    /**
     * Constant defining the role identifier for administrators within the system.
     * This role grants access to administrative functionalities and permissions.
     */
    public final static String RULE_ADMINISTRATORS = "ROLE_ADMINISTRATORS";
    /**
     * Constants for the context key used to store CSRF token information.
     * This string represents the identifier used to retrieve or store CSRF tokens in a context such as a thread local,
     * request attribute, or any other data structure where keys are strings.
     */
    public final static String CSRF_TOKEN_CONTEXT = "CSRF_TOKEN_CONTEXT";
    /**
     * A constant holding a pre-initialized instance of {@link MessageDigest}.
     * This digest can be used for cryptographic hashing operations.
     */
    public final static MessageDigest MD;
    /**
     * An array of strings representing the possible header names that could contain
     * the client's IP address in HTTP requests. This is typically used when working
     * behind proxies or load balancers where the original client IP is not directly
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

    static {
        try {
            MD = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw RestServerException.withMsg("MD5 algorithm not found", e);
        }
    }
    /**
     * A shared static instance of Jackson's {@link ObjectMapper}, which is utilized for
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
    public static ObjectMapper OBJECT_MAPPER;
    /**
     * Static reference to the UsersService instance.
     * This service provides functionalities related to user management such as
     * user retrieval, creation, update, and deletion.
     */
    public static UsersService USERS_SERVICE;

    /**
     * Initializes the ContextUtils class with necessary dependencies.
     *
     * @param objectMapper The ObjectMapper instance used for JSON serialization and deserialization.
     * @param usersService The UsersService instance to provide access to user-related operations.
     */
    ContextUtils(ObjectMapper objectMapper, UsersService usersService) {
        ContextUtils.OBJECT_MAPPER = objectMapper;
        ContextUtils.USERS_SERVICE = usersService;
    }

    /**
     * Encodes the given input string into its MD5 hash representation, which is then
     * base64 encoded for a more compact and URL-friendly format.
     *
     * @param input The string to be encoded into MD5 hash.
     * @return A base64 encoded string representing the MD5 hash of the input string.
     */
    public static String encodeToMD5(String input) {
        byte[] bytes = MD.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Retrieves the client's IP address from the given server HTTP request.
     * This method iterates over a set of common headers used to carry the client's IP address
     * and returns the first non-empty value found. If none of these headers yield an IP,
     * it falls back to extracting the IP from the remote address of the request.
     *
     * @param httpRequest The server HTTP request from which to extract the client's IP address.
     * @return The client's IP address as a string, or the remote address's host address if no
     * explicit IP is found in the headers.
     */
    public static String getClientIpAddress(ServerHttpRequest httpRequest) {
        HttpHeaders headers = httpRequest.getHeaders();
        for (String header : IP_HEADER_CANDIDATES) {
            List<String> ipList = headers.get(header);
            if (ipList != null && !ipList.isEmpty()) {
                return ipList.getFirst();
            }
        }
        return Objects.requireNonNull(httpRequest.getRemoteAddress()).getAddress().getHostAddress();
    }

    /**
     * Retrieves the security details of the currently authenticated user.
     * <p>
     * This method accesses the security context asynchronously and extracts the principal,
     * which is then cast to a {@link SecurityDetails} object. It is designed to be used
     * within a reactive environment where security information is required for further processing.
     * </p>
     *
     * @return A {@link Mono} emitting the {@link SecurityDetails} associated with the
     * current authentication context, or an empty Mono if no authentication is present.
     */
    public static Mono<SecurityDetails> securityDetails() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getPrincipal())
                .cast(SecurityDetails.class);
    }

    /**
     * Serializes the {@link UserAuditor} objects contained within the properties of the given input object.
     * This method inspects the object's properties to identify those of type {@link UserAuditor} and applies
     * a serialization process to them. It is designed to work with objects that may contain auditor fields
     * which need to be processed before further operations, such as saving to a database or sending over a network.
     *
     * @param <T>    The generic type of the object whose properties are to be serialized.
     * @param object The object whose properties are to be inspected and potentially serialized.
     * @return A {@link Mono} wrapping the original object after all {@link UserAuditor} properties have been processed.
     * The Mono completes when all processing is finished.
     */
    public static <T> Mono<T> serializeUserAuditor(T object) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(object.getClass());
        var propertyFlux = Flux.fromArray(propertyDescriptors)
                .filter(propertyDescriptor -> propertyDescriptor.getPropertyType() == UserAuditor.class)
                .flatMap(propertyDescriptor -> serializeUserAuditorProperty(object, propertyDescriptor));
        return propertyFlux.then(Mono.just(object));
    }

    /**
     * Handles the property descriptor for an object, particularly focusing on serializing
     * a {@link UserAuditor} within the given object by fetching additional user details
     * and updating the object accordingly.
     *
     * @param <T>                The type of the object containing the property to be handled.
     * @param object             The object whose property described by {@code propertyDescriptor} is to be processed.
     * @param propertyDescriptor The descriptor of the property within {@code object} that refers to a {@link UserAuditor}.
     * @return A {@link Mono} emitting a message indicating success or failure:
     * - If the user auditor is empty, emits a warning message and completes.
     * - If successful in serializing the user auditor, emits a success message and completes.
     * - Emits an error signal if there are issues invoking methods on the property descriptor.
     */
    private static <T> Mono<String> serializeUserAuditorProperty(T object, PropertyDescriptor propertyDescriptor) {
        try {
            UserAuditor userAuditor = (UserAuditor) propertyDescriptor.getReadMethod().invoke(object);
            if (ObjectUtils.isEmpty(userAuditor)) {
                String msg = "User auditor is empty, No serializable." + propertyDescriptor.getName();
                log.warn(msg);
                return Mono.just(msg);
            }
            return USERS_SERVICE.loadByCode(userAuditor.code()).cache().flatMap(user -> {
                try {
                    propertyDescriptor.getWriteMethod().invoke(object, UserAuditor.withUser(user));
                    String msg = "User auditor serializable success. " + propertyDescriptor.getName();
                    log.debug(msg);
                    return Mono.just(msg);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    return Mono.error(RestServerException.withMsg(
                            "User auditor serialization getWriteMethod invoke error!", e));
                }
            });
        } catch (IllegalAccessException | InvocationTargetException e) {
            return Mono.error(RestServerException.withMsg(
                    "User auditor serialization getReadMethod invoke error!", e));
        }
    }

    /**
     * Generates the next unique identifier using ULID (Universally Unique Lexicographically Sortable Identifier).
     * <p>
     * This method utilizes the {@link UlidCreator#getMonotonicUlid()} to produce ULIDs which are:
     * - Globally unique across space and time.
     * - Lexicographically sortable.
     * - Monotonic in time.
     * </p>
     *
     * @return A string representation of the generated ULID, which can be used as a unique identifier.
     */
    public static String nextId() {
        Ulid ulid = UlidCreator.getMonotonicUlid();
        return ulid.toString();
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
    }
}