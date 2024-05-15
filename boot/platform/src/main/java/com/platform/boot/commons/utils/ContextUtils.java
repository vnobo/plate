package com.platform.boot.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.boot.commons.exception.JsonException;
import com.platform.boot.commons.exception.RestServerException;
import com.platform.boot.commons.ulid.Ulid;
import com.platform.boot.security.SecurityDetails;
import com.platform.boot.security.core.UserAuditor;
import com.platform.boot.security.core.user.UsersService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Log4j2
@Component
public final class ContextUtils implements InitializingBean {
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

    public final static String RULE_ADMINISTRATORS = "ROLE_ADMINISTRATORS";
    public final static String CSRF_TOKEN_CONTEXT = "CSRF_TOKEN_CONTEXT";

    public static ObjectMapper OBJECT_MAPPER;
    public static UsersService USERS_SERVICE;

    ContextUtils(ObjectMapper objectMapper, UsersService usersService) {
        ContextUtils.OBJECT_MAPPER = objectMapper;
        ContextUtils.USERS_SERVICE = usersService;
    }

    public static byte[] objectToBytes(Object object) {
        try {
            return ContextUtils.OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw JsonException.withError(e);
        }
    }

    public static String getClientIpAddress(ServerHttpRequest httpRequest) {
        HttpHeaders headers = httpRequest.getHeaders();
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = headers.getFirst(header);
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                String[] ipArray = StringUtils.commaDelimitedListToStringArray(ipList);
                return ipArray[0];
            }
        }
        return Objects.requireNonNull(httpRequest.getRemoteAddress()).getAddress().getHostAddress();
    }

    public static Mono<SecurityDetails> securityDetails() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getPrincipal())
                .cast(SecurityDetails.class);
    }

    public static <T> Mono<T> serializeUserAuditor(T object) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(object.getClass());
        var setPropertyStream = Arrays.stream(propertyDescriptors)
                .filter(propertyDescriptor -> propertyDescriptor.getPropertyType() == UserAuditor.class);
        var propertyFlux = Flux.fromStream(setPropertyStream)
                .flatMap(propertyDescriptor -> handlePropertyDescriptor(object, propertyDescriptor));
        return propertyFlux.then(Mono.just(object));
    }

    private static <T> Mono<String> handlePropertyDescriptor(T object, PropertyDescriptor propertyDescriptor) {
        try {
            UserAuditor userAuditor = (UserAuditor) propertyDescriptor.getReadMethod().invoke(object);
            if (ObjectUtils.isEmpty(userAuditor)) {
                return Mono.just("User auditor is empty, No serializable." + propertyDescriptor.getName());
            }
            return USERS_SERVICE.loadByCode(userAuditor.code()).flatMap(user -> {
                try {
                    propertyDescriptor.getWriteMethod().invoke(object, UserAuditor.withUser(user));
                    return Mono.just("User auditor serializable success. " + propertyDescriptor.getName());
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

    public static String nextId() {
        return Ulid.random();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("ContextUtils Initializing ...");
    }
}