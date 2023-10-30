package com.platform.boot.relational.logger;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class LoggersService extends AbstractDatabase {

    private final LoggersRepository loggersRepository;

    public Flux<Logger> search(LoggerRequest request, Pageable pageable) {
        //Create a cache key based on the given request and pageable parameters
        var cacheKey = ContextUtils.cacheKey(request, pageable);
        //Create a query based on the given request and pageable parameters
        Query query = Query.query(request.toCriteria()).with(pageable);
        //Return the query with the cache key and Logger class
        return this.queryWithCache(cacheKey, query, Logger.class);
    }

    public Mono<Page<Logger>> page(LoggerRequest request, Pageable pageable) {
        //Create a cache key based on the request
        var cacheKey = ContextUtils.cacheKey(request);
        //Create a query based on the request
        Query query = Query.query(request.toCriteria());
        //Collect a list of Loggers based on the request and pageable
        var searchMono = this.search(request, pageable).collectList();
        //Count the number of Loggers based on the cache key, query, and Logger class
        var countMono = this.countWithCache(cacheKey, query, Logger.class);
        //Return a Mono of a Page of Loggers based on the searchMono and countMono
        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<Logger> operate(LoggerRequest request) {
        return this.save(request.toLogger()).doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<Logger> save(Logger logger) {
        if (logger.isNew()) {
            return this.loggersRepository.save(logger);
        } else {
            assert logger.getId() != null;
            return this.loggersRepository.findById(logger.getId()).flatMap(old -> {
                logger.setCreatedTime(old.getCreatedTime());
                return this.loggersRepository.save(logger);
            });
        }
    }
}