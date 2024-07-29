package com.plate.boot.relational.logger;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class LoggersService extends AbstractDatabase {

    private final LoggersRepository loggersRepository;

    public Flux<Logger> search(LoggerRequest request, Pageable pageable) {
        Query query = Query.query(request.toCriteria()).with(pageable);
        return this.queryWithCache(BeanUtils.cacheKey(request, pageable), query, Logger.class);
    }

    public Mono<Page<Logger>> page(LoggerRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        var countMono = this.countWithCache(BeanUtils.cacheKey(request),
                Query.query(request.toCriteria()), Logger.class);
        return searchMono.zipWith(countMono)
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

    @Scheduled(cron = "0 0 1 * * ?")
    public void clearLoggers() {
        this.loggersRepository.deleteByCreatedTimeBefore(LocalDateTime.now().minusYears(3))
                .subscribe(res -> log.info("清理过期日志: {}", res));
    }
}