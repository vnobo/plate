package com.plate.boot.relational.logger;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        String querySql = "select * from se_loggers";
        QueryFragment params = request.buildQueryFragment();
        var cacheKey = BeanUtils.cacheKey(request, pageable);
        var query = querySql + params.whereSql() + QueryHelper.applyPage(pageable);
        return this.queryWithCache(cacheKey, query, params, Logger.class);
    }

    public Mono<Page<Logger>> page(LoggerRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        String querySql = "select count(*) from se_loggers";
        var fragment = request.buildQueryFragment();
        var query = querySql + fragment.whereSql();
        var countMono = this.countWithCache(BeanUtils.cacheKey(request), query, fragment);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * 操作日志记录器请求，将请求转换为日志记录器对象，保存并在完成后清除缓存。
     *
     * @param request 请求对象，包含创建或更新日志记录器所需的数据。
     * @return 一个 Mono<Logger> 对象，代表保存操作的结果。
     */
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