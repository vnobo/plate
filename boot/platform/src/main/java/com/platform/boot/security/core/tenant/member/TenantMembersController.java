package com.platform.boot.security.core.tenant.member;


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
    public Flux<TenantMemberResponse> search(TenantMemberRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.tenantMembersService.search(request
                        .securityCode(securityDetails.getTenantCode()), pageable));
    }

    @GetMapping("page")
    public Mono<Page<TenantMemberResponse>> page(TenantMemberRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                this.tenantMembersService.page(request
                        .securityCode(securityDetails.getTenantCode()), pageable));
    }

    @PostMapping("save")
    public Mono<TenantMember> save(@Valid @RequestBody TenantMemberRequest request) {
        return this.tenantMembersService.operate(request);
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody TenantMemberRequest request) {
        Assert.notNull(request.getId(), "When deleting a Tenant, the ID must not be null");
        return this.tenantMembersService.delete(request);
    }

}