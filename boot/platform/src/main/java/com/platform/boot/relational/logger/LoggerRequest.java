package com.platform.boot.relational.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoggerRequest extends Logger {

    private String securityCode;

    public static LoggerRequest of(String tenantCode, String operator, String prefix, String method,
                                   String status, String url, JsonNode context) {
        LoggerRequest request = new LoggerRequest();
        request.setTenantCode(tenantCode);
        request.setOperator(operator);
        request.setPrefix(prefix);
        request.setUrl(url);
        request.setMethod(method);
        request.setStatus(status);
        request.setContext(context);
        return request;
    }

    public Logger toLogger() {
        return BeanUtils.copyProperties(this, Logger.class);
    }

    public Criteria toCriteria() {

        Criteria criteria = criteria(Set.of("securityCode", "context"));

        if (StringUtils.hasLength(this.getSecurityCode())) {
            criteria = criteria.and("tenantCode").is(this.getSecurityCode());
        }

        return criteria;
    }

}