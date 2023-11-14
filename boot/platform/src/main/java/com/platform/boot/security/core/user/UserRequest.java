package com.platform.boot.security.core.user;

import com.platform.boot.commons.query.ParamSql;
import com.platform.boot.commons.query.QueryJson;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;


/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRequest extends User {

    private Map<String, Object> query;

    public User toUser() {
        return BeanUtils.copyProperties(this, User.class);
    }

    public ParamSql bindParamSql() {
        ParamSql rescues = CriteriaUtils.buildParamSql(this, List.of("query"), null);
        var params = rescues.params();
        var sql = rescues.sql();
        ParamSql jsonParamSql = QueryJson.queryJson(this.getQuery());
        params.putAll(jsonParamSql.params());
        sql.merge(jsonParamSql.sql());
        return ParamSql.of(sql, params);
    }
}