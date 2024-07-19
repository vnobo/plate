package com.platform.boot.relational.logger;

import com.platform.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/loggers")
@RequiredArgsConstructor
public class LoggersController {

    private final LoggersService loggersService;

    @GetMapping("page")
    public Mono<PagedModel<Logger>> page(LoggerRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(userDetails -> {
            request.setSecurityCode(userDetails.getTenantCode());
            return this.loggersService.page(request, pageable);
        }).map(PagedModel::new);
    }

}