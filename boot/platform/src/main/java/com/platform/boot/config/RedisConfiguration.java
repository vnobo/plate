package com.platform.boot.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * @author Alex Bob (<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisConfiguration {
}