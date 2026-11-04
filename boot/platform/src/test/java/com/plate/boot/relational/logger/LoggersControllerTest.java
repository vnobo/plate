package com.plate.boot.relational.logger;

import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static com.plate.boot.commons.utils.ContextUtils.DEFAULT_UUID_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoggersController}.
 */
@ExtendWith(MockitoExtension.class)
class LoggersControllerTest {

    @Mock
    private LoggersService loggersService;

    @InjectMocks
    private LoggersController controller;

    private static SecurityDetails securityDetails() {
        return new SecurityDetails(List.of(), Map.of("username", "admin"), "username");
    }

    @Test
    void pageStampsSecurityCodeAndWrapsInPagedModel() {
        LoggerReq request = new LoggerReq();
        Pageable pageable = PageRequest.of(0, 20);

        LoggerRes res = new LoggerRes();
        res.setPrefix("SEC");
        Page<LoggerRes> page = new PageImpl<>(List.of(res), pageable, 1);
        when(loggersService.page(any(LoggerReq.class), any(Pageable.class))).thenReturn(Mono.just(page));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(securityDetails());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        StepVerifier.create(controller.page(request, pageable)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .assertNext(paged -> {
                    assertThat(paged).isInstanceOf(PagedModel.class);
                    assertThat(paged.getContent()).containsExactly(res);
                })
                .verifyComplete();

        verify(loggersService).page(request, pageable);
        assertThat(request.getSecurityCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void pageCompletesEmptyWithoutSecurityContext() {
        LoggerReq request = new LoggerReq();
        Pageable pageable = PageRequest.of(0, 20);

        StepVerifier.create(controller.page(request, pageable)).verifyComplete();

        verify(loggersService, never()).page(any(LoggerReq.class), any(Pageable.class));
    }
}
