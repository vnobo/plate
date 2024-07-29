package com.plate.auth.commons.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public abstract class AbstractDatabase extends AbstractService {

    protected JdbcClient jdbcClient;
    protected JdbcTemplate jdbcTemplate;
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    protected ConversionService conversionService;

    @Autowired
    public void setJdbcClient(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Autowired
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Autowired
    public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }
}