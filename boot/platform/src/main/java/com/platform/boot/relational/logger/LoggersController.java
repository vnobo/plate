package com.platform.boot.relational.logger;

import com.platform.boot.commons.utils.ContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/loggers")
@RequiredArgsConstructor
public class LoggersController {

    private final LoggersService loggersService;

    @GetMapping("search")
    public Flux<Logger> search(LoggerRequest request, Pageable pageable) {
        return ContextHolder.securityDetails().flatMapMany(userDetails -> {
            request.setSecurityCode(userDetails.getTenantCode());
            return this.loggersService.search(request, pageable);
        });
    }

    @GetMapping("page")
    public Mono<Page<Logger>> page(LoggerRequest request, Pageable pageable) {
        return ContextHolder.securityDetails().flatMap(userDetails -> {
            request.setSecurityCode(userDetails.getTenantCode());
            return this.loggersService.page(request, pageable);
        });
    }

}