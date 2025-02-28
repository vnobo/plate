package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller for handling tenant member operations.
 * Provides endpoints for searching, paging, saving, and deleting tenant members.
 * <p>
 * Author: <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/tenants/members")
@RequiredArgsConstructor
public class TenantMembersController {

    private final TenantMembersService tenantMembersService;

    /**
     * Searches for tenant members based on the provided request and pageable parameters.
     *
     * @param request  the tenant member request containing search criteria
     * @param pageable the pagination information
     * @return a Flux of TenantMemberRes objects matching the search criteria
     */
    @GetMapping("search")
    public Flux<TenantMemberRes> search(TenantMemberReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.tenantMembersService.search(request
                        .securityCode(securityDetails.getTenantCode()), pageable));
    }

    /**
     * Retrieves a paginated list of tenant members based on the provided request and pageable parameters.
     *
     * @param request  the tenant member request containing search criteria
     * @param pageable the pagination information
     * @return a Mono of PagedModel containing TenantMemberRes objects
     */
    @GetMapping("page")
    public Mono<PagedModel<TenantMemberRes>> page(TenantMemberReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                        this.tenantMembersService.page(request
                                .securityCode(securityDetails.getTenantCode()), pageable))
                .map(PagedModel::new);
    }

    /**
     * Saves a tenant member based on the provided request.
     *
     * @param request the tenant member request containing the details to save
     * @return a Mono of the saved TenantMember object
     */
    @PostMapping("save")
    public Mono<TenantMember> save(@Valid @RequestBody TenantMemberReq request) {
        return this.tenantMembersService.operate(request);
    }

    /**
     * Deletes a tenant member based on the provided request.
     *
     * @param request the tenant member request containing the ID of the tenant member to delete
     * @return a Mono indicating completion of the delete operation
     * @throws IllegalArgumentException if the ID is null
     */
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody TenantMemberReq request) {
        Assert.notNull(request.getId(), "When deleting a Tenant, the ID must not be null");
        return this.tenantMembersService.delete(request);
    }

}