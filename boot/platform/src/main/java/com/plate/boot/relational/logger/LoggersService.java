package com.plate.boot.relational.logger;

import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Service class responsible for handling operations related to loggers, including searching,
 * paging, saving, and scheduled cleanup of outdated log records.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class LoggersService extends AbstractCache {

    private final LoggersRepository loggersRepository;

    /**
     * Searches for loggers based on the provided request and pagination information.
     *
     * @param request  A LoggerReq object containing criteria for filtering loggers.
     * @param pageable Pagination details defining how the results should be sliced.
     * @return A Flux of Logger objects matching the search criteria, respecting the specified pagination.
     */
    public Flux<Logger> search(LoggerReq request, Pageable pageable) {
        QueryFragment queryFragment = QueryHelper.query(request, pageable);
        var cacheKey = BeanUtils.cacheKey(request, pageable);
        return this.queryWithCache(cacheKey, queryFragment.querySql(), queryFragment, Logger.class);
    }

    /**
     * Retrieves a paginated list of loggers based on the provided request and pagination details.
     *
     * @param request  A {@link LoggerReq} object containing criteria to filter loggers.
     * @param pageable A {@link Pageable} instance specifying pagination information like page number, size, sorting, etc.
     * @return A {@link Mono} emitting a {@link Page} of {@link Logger} objects that match the given criteria,
     * respecting the specified pagination and sorted accordingly. The {@link Page} includes both content and
     * metadata such as total elements, page number, and page size.
     */
    public Mono<Page<Logger>> page(LoggerReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment queryFragment = QueryHelper.query(request, pageable);
        var countMono = this.countWithCache(BeanUtils.cacheKey(request), queryFragment.countSql(), queryFragment);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * Operates on a given {@link LoggerReq} by converting it into a {@link Logger}
     * entity and saving it using the {@link #save(Logger)} method. After the termination
     * of the save operation, the cache is cleared to ensure fresh data is fetched on subsequent queries.
     *
     * @param request The {@link LoggerReq} containing details necessary to create or update a {@link Logger} entity.
     * @return A {@link Mono} emitting the saved {@link Logger} entity upon successful completion of the save operation.
     */
    public Mono<Logger> operate(LoggerReq request) {
        return this.save(request.toLogger()).doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Persists a {@link Logger} entity to the database. Determines whether the operation
     * should be an insert or an update based on the entity's state. If the logger is deemed
     * new (its ID is not set), it will be inserted. Otherwise, it fetches the existing
     * record from the database, preserves the creation timestamp, and updates the record.
     *
     * @param logger The {@link Logger} entity to save. Must not be {@code null}.
     * @return A {@link Mono} emitting the saved {@link Logger} entity after the operation completes.
     * Emits the entity whether it was inserted or updated.
     * @throws IllegalArgumentException if the {@code logger} is {@code null}.
     */
    public Mono<Logger> save(Logger logger) {
        if (logger.isNew()) {
            return this.loggersRepository.save(logger);
        } else {
            assert logger.getId() != null;
            return this.loggersRepository.findById(logger.getId()).flatMap(old -> {
                logger.setCreatedAt(old.getCreatedAt());
                return this.loggersRepository.save(logger);
            });
        }
    }

/**
 * Scheduled task to clear outdated log records.
 * <p>
 * This method is scheduled to run daily at 1 AM. It deletes log records that are older than three years
 * from the current date. The result of the deletion operation is logged.
 * </p>
 */
@Scheduled(cron = "0 0 1 * * ?")
public void clearLoggers() {
    this.loggersRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusYears(3))
            .subscribe(res -> log.info("CLEAN UP EXPIRED LOGS: {}", res));
}

/**
 * Event listener for processing logger events.
 * <p>
 * This method listens for {@link LoggerEvent} instances. If the event kind is INSERT, it converts the event's
 * entity to a {@link Logger} and saves it using the {@link #operate(LoggerReq)} method. The result of the save
 * operation is logged.
 * </p>
 *
 * @param event The {@link LoggerEvent} to process.
 */
@EventListener
public void processLoggerEvent(LoggerEvent event) {
    if (event.kind() == LoggerEvent.Kind.INSERT) {
        var logger = event.entity();
        this.operate(logger).subscribe(res ->
                log.debug("Client request log save result. log: {}", logger.getContext()));
    }
}
}