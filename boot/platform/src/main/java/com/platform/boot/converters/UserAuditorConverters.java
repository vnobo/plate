package com.platform.boot.converters;

import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.UserAuditor;
import com.platform.boot.security.user.User;
import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * This class contains converters for UserAuditor objects.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
public class UserAuditorConverters {

    @Component
    @WritingConverter
    public static class UserAuditorWriteConverter implements Converter<UserAuditor, String> {
        /**
         * Converts a UserAuditor object to a string.
         *
         * @param source the UserAuditor object to convert
         * @return the username of the UserAuditor object
         */
        @Override
        public String convert(@NonNull UserAuditor source) {
            return source.getUsername();
        }
    }

    @Component
    @ReadingConverter
    public static class UserAuditorReadConverter implements Converter<String, UserAuditor> {

        private final Cache cache;

        public UserAuditorReadConverter(CacheManager cacheManager) {
            this.cache = cacheManager.getCache("user-auditor-cache");
            if (this.cache != null) {
                this.cache.clear();
            }
        }

        /**
         * Converts a string to a UserAuditor object.
         *
         * @param source the string to convert
         * @return a UserAuditor object with the given username
         */
        @Override
        public UserAuditor convert(@NonNull String source) {
            UserAuditor userAuditor = UserAuditor.withUsername(source);
            User user = queryWithCache(source).share().block();
            if (!ObjectUtils.isEmpty(user)) {
                userAuditor.setName(user.getName());
            }
            return userAuditor;
        }

        private Mono<User> queryWithCache(String username) {
            assert this.cache != null;

            // Get data from cache
            User cacheData = this.cache.get(username, () -> null);

            Query query = Query.query(Criteria.where("username").is(username).ignoreCase(true)).limit(1);
            // Construct the query request and sort the results
            Mono<User> source = ContextUtils.ENTITY_TEMPLATE.select(query, User.class).shareNext()
                    .subscribeOn(Schedulers.single())
                    // Add the query result to the cache
                    .doOnNext(user -> this.cache.put(username, user));
            // If there is no data in the cache, return the query result; otherwise, return the cache data
            return Mono.justOrEmpty(cacheData).switchIfEmpty(source);
        }
    }
}