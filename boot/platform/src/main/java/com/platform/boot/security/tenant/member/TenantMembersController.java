package com.platform.boot.security.tenant.member;


import com.platform.boot.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/tenants/members")
@RequiredArgsConstructor
public class TenantMembersController {

    private final TenantMembersService tenantMembersService;

    @GetMapping("search")
    public Flux<TenantMember> search(TenantMemberRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.tenantMembersService.search(request
                        .securityCode(securityDetails.getTenantCode()), pageable));
    }

    @GetMapping("page")
    public Mono<Page<TenantMember>> page(TenantMemberRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                this.tenantMembersService.page(request
                        .securityCode(securityDetails.getTenantCode()), pageable));
    }

    // Endpoint to add a Tenant
    @PostMapping("add")
    public Mono<TenantMember> add(@Valid @RequestBody TenantMemberRequest request) {
        // Check that the Tenant ID is null (i.e. this is a new Tenant)
        Assert.isTrue(request.isNew(), "When adding a new Tenant, the ID must be null");
        // Call the Tenants service to add the Tenant and return the result as a Mono
        return this.tenantMembersService.operate(request);
    }

    // Endpoint to modify a Tenant
    @PutMapping("modify")
    public Mono<TenantMember> modify(@Valid @RequestBody TenantMemberRequest request) {
        // Check that the Tenant ID is not null (i.e. this is an existing Tenant)
        Assert.isTrue(!request.isNew(), "When modifying an existing Tenant, the ID must not be null");
        // Call the Tenants service to modify the Tenant and return the result as a Mono
        return this.tenantMembersService.operate(request);
    }

    // Endpoint to delete a Tenant
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody TenantMemberRequest request) {
        // Check that the Tenant ID is not null (i.e. this is an existing Tenant)
        Assert.isTrue(!request.isNew(), "When deleting a Tenant, the ID must not be null");
        // Call the Tenants service to delete the Tenant and return the result as a Mono
        return this.tenantMembersService.delete(request);
    }

}