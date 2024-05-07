package com.platform.boot.relational.logger;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.BeanUtils;
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
        var cacheKey = BeanUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable);
        return this.queryWithCache(cacheKey, query, Logger.class);
    }

    public Mono<Page<Logger>> page(LoggerRequest request, Pageable pageable) {
        var cacheKey = BeanUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        var searchMono = this.search(request, pageable).collectList();
        var countMono = this.countWithCache(cacheKey, query);
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