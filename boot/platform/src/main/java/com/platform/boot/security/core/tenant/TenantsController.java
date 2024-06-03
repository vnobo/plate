package com.platform.boot.security.core.tenant;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantsController {

    private final TenantsService tenantsService;

    @GetMapping("search")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Flux<Tenant> search(TenantRequest request, Pageable pageable) {
        return this.tenantsService.search(request, pageable);
    }

    @GetMapping("page")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Page<Tenant>> page(TenantRequest request, Pageable pageable) {
        return this.tenantsService.page(request, pageable);
    }

    @PostMapping("save")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Tenant> operate(@Valid @RequestBody TenantRequest request) {
        return this.tenantsService.operate(request);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Void> delete(@Valid @RequestBody TenantRequest request) {
        Assert.notNull(request.getId(), "When deleting a Tenant, the ID must not be null");
        return this.tenantsService.delete(request);
    }

}