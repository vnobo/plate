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

    @PostMapping("add")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Tenant> add(@Valid @RequestBody TenantRequest request) {
        // Check that the Tenant ID is null (i.e. this is a new Tenant)
        Assert.isTrue(request.isNew(), "When adding a new Tenant, the ID must be null");
        // Call the Tenants service to add the Tenant and return the result as a Mono
        return this.tenantsService.operate(request);
    }

    @PutMapping("modify")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Tenant> modify(@Valid @RequestBody TenantRequest request) {
        // Check that the Tenant ID is not null (i.e. this is an existing Tenant)
        Assert.isTrue(!request.isNew(), "When modifying an existing Tenant, the ID must not be null");
        // Call the Tenants service to modify the Tenant and return the result as a Mono
        return this.tenantsService.operate(request);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Void> delete(@Valid @RequestBody TenantRequest request) {
        // Check that the Tenant ID is not null (i.e. this is an existing Tenant)
        Assert.isTrue(!request.isNew(), "When deleting a Tenant, the ID must not be null");
        // Call the Tenants service to delete the Tenant and return the result as a Mono
        return this.tenantsService.delete(request);
    }

}