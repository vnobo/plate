package com.plate.boot.relational.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.CriteriaUtils;
import com.plate.boot.commons.utils.query.ParamSql;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoggerRequest extends Logger {

    private Map<String, Object> query;

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
    public ParamSql bindParamSql() {
        return CriteriaUtils.buildParamSql(this, List.of(), null);
    }
    public Criteria toCriteria() {

        Criteria criteria = criteria(Set.of("securityCode", "context","query"));

        if (StringUtils.hasLength(this.getSecurityCode())) {
            criteria = criteria.and("tenantCode").is(this.getSecurityCode());
        }

        return criteria;
    }

}