package com.plate.boot.security.core.tenant;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
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
    public Flux<Tenant> search(TenantRequest request, Pageable pageable) {
        return this.tenantsService.search(request, pageable);
    }

    @GetMapping("page")
    public Mono<PagedModel<Tenant>> page(TenantRequest request, Pageable pageable) {
        return this.tenantsService.page(request, pageable).map(PagedModel::new);
    }

    @PostMapping("save")
    public Mono<Tenant> operate(@Valid @RequestBody TenantRequest request) {
        return this.tenantsService.operate(request);
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody TenantRequest request) {
        Assert.notNull(request.getId(), "When deleting a Tenant, the ID must not be null");
        return this.tenantsService.delete(request);
    }

}