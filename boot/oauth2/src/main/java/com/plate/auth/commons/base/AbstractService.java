package com.plate.auth.commons.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
public abstract class AbstractService implements InitializingBean {

    protected ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterPropertiesSet() {
        log.debug("Initializing provider names: {}",this.getClass().getName());
    }
}