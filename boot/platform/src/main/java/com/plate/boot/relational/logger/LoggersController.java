package com.plate.boot.relational.logger;

import com.plate.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller responsible for handling CRUD and pagination operations on loggers.
 * This controller interacts with the {@link LoggersService} to process requests
 * related to fetching, creating, updating log records, and managing their pagination.
 */
@RestController
@RequestMapping("/loggers")
@RequiredArgsConstructor
public class LoggersController {

    private final LoggersService loggersService;

    /**
     * Retrieves a paginated list of log records based on the provided request and pagination details.
     * <p>
     * This method first extracts the security details of the currently authenticated user to enrich
     * the request with a tenant code. It then delegates the pagination logic to the {@link LoggersService},
     * which processes the request further to fetch the relevant log entries. The returned data is wrapped
     * in a {@link Page} to facilitate standardized pagination responses.
     *
     * @param request  A {@link LoggerReq} object encapsulating the criteria for the log search,
     *                 such as filters, prefixes, or additional metadata.
     * @param pageable A Spring {@link Pageable} instance defining the pagination parameters like page number,
     *                 size, sorting instructions, etc.
     * @return A {@link Mono} emitting a {@link Page} containing a page of {@link Logger} objects that
     * match the specified criteria and pagination settings. The {@link Page} includes metadata about
     * the current page and provides access to the actual log records.
     */
    @GetMapping("page")
    public Mono<Page<Logger>> page(LoggerReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(userDetails -> {
            request.setSecurityCode(userDetails.getTenantCode());
            return this.loggersService.page(request, pageable);
        });
    }
}