package com.plate.auth.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.plate.auth.commons.exception.JsonException;
import com.plate.auth.commons.exception.RestServerException;
import com.plate.auth.security.SecurityDetails;
import com.plate.auth.security.core.UserAuditor;
import com.plate.auth.security.core.user.User;
import com.plate.auth.security.core.user.UsersService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Log4j2
@Component
public final class ContextUtils implements InitializingBean {

    public final static String RULE_ADMINISTRATORS = "ROLE_ADMINISTRATORS";
    public final static String CSRF_TOKEN_CONTEXT = "CSRF_TOKEN_CONTEXT";
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
    }  public static String objectToString(Object object) {
        try {
            return ContextUtils.OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw JsonException.withError(e);
        }
    }

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

    public static SecurityDetails securityDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return (SecurityDetails) securityContext.getAuthentication().getPrincipal();
    }

    public static <T> T serializeUserAuditor(T object) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(object.getClass());
        var setPropertyStream = Arrays.stream(propertyDescriptors)
                .filter(propertyDescriptor -> propertyDescriptor.getPropertyType() == UserAuditor.class)
                .collect(Collectors.toSet());
        for (var propertyDescriptor : setPropertyStream) {
            String msg = handlePropertyDescriptor(object, propertyDescriptor);
            log.debug("handlePropertyDescriptor: {}", msg);
        }
        return object;
    }

    private static <T> String handlePropertyDescriptor(T object, PropertyDescriptor propertyDescriptor) {
        try {
            UserAuditor userAuditor = (UserAuditor) propertyDescriptor.getReadMethod().invoke(object);
            if (ObjectUtils.isEmpty(userAuditor)) {
                String msg = "User auditor is empty, No serializable." + propertyDescriptor.getName();
                log.warn(msg);
                return msg;
            }
            User user = USERS_SERVICE.loadByCode(userAuditor.code());
            propertyDescriptor.getWriteMethod().invoke(object, UserAuditor.withUser(user));
            String msg = "User auditor serializable success. " + propertyDescriptor.getName();
            log.debug(msg);
            return msg;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw RestServerException.withMsg(
                    "User auditor serialization getReadMethod invoke error!", e);
        }
    }

    public static String nextId() {
        Ulid ulid = UlidCreator.getMonotonicUlid();
        return ulid.toString();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing utils [ContextUtils]...");
    }
}