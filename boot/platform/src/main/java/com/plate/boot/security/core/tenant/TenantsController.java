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
 * REST controller for managing tenants.
 * This class provides endpoints for searching, paginating, operating, and deleting tenants.
 * It uses reactive programming with Project Reactor.
 * <p>
 * The class is annotated with \@RestController to indicate that it's a REST controller in the Spring context.
 * It is also annotated with \@RequestMapping to map HTTP requests to handler methods.
 * \@RequiredArgsConstructor is used to generate a constructor with required arguments.
 * <p>
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantsController {

    /**
     * The service for managing tenants.
     */
    private final TenantsService tenantsService;

    /**
     * Searches for tenants based on the given request and pageable parameters.
     *
     * @param request  the tenant request containing search criteria
     * @param pageable the pagination information
     * @return a Flux emitting the tenants that match the search criteria
     */
    @GetMapping("search")
    public Flux<Tenant> search(TenantReq request, Pageable pageable) {
        return this.tenantsService.search(request, pageable);
    }

    /**
     * Paginates the tenants based on the given request and pageable parameters.
     *
     * @param request  the tenant request containing search criteria
     * @param pageable the pagination information
     * @return a Mono emitting a PagedModel of tenants that match the search criteria
     */
    @GetMapping("page")
    public Mono<PagedModel<Tenant>> page(TenantReq request, Pageable pageable) {
        return this.tenantsService.page(request, pageable).map(PagedModel::new);
    }

    /**
     * Operates on a tenant based on the given request.
     * If the tenant exists, it updates the tenant; otherwise, it creates a new tenant.
     *
     * @param request the tenant request containing tenant information
     * @return a Mono emitting the operated tenant
     */
    @PostMapping("save")
    public Mono<Tenant> operate(@Valid @RequestBody TenantReq request) {
        return this.tenantsService.operate(request);
    }

    /**
     * Deletes a tenant based on the given request.
     * It deletes the tenant and its associated members.
     *
     * @param request the tenant request containing tenant information
     * @return a Mono indicating when the deletion is complete
     */
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody TenantReq request) {
        Assert.notNull(request.getId(), "When deleting a Tenant, the ID must not be null");
        return this.tenantsService.delete(request);
    }

}