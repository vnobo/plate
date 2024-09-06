package com.plate.boot.relational.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoggerRequest extends Logger {

    private Map<String, Object> query;

    private String securityCode;

    public static LoggerRequest of(String tenantCode, String operator, String prefix,
                                   String method, String status, String url, JsonNode context) {
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

    public QueryFragment buildQueryFragment() {
        QueryFragment fragment = QueryHelper.query(this, List.of("operator"), null);
        StringJoiner criteria = fragment.sql();
        Map<String, Object> params = fragment.params();
        if (!ObjectUtils.isEmpty(this.getOperator())) {
            criteria.add("operator in (:operator)");
            params.put("operator", Arrays.asList("a", "b"));
        }
        return QueryFragment.of(criteria, params);
    }
}