package com.platform.boot.security.group.member;


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
@RequestMapping("/groups/members")
@RequiredArgsConstructor
public class GroupMembersController {

    private final GroupMembersService groupMembersService;

    @GetMapping("search")
    public Flux<GroupMemberResponse> search(GroupMemberRequest request, Pageable pageable) {
        return this.groupMembersService.search(request, pageable);
    }

    @GetMapping("page")
    public Mono<Page<GroupMemberResponse>> page(GroupMemberRequest request, Pageable pageable) {
        return this.groupMembersService.page(request, pageable);
    }

    @PostMapping("save")
    public Mono<GroupMember> save(@Valid @RequestBody GroupMemberRequest request) {
        return this.groupMembersService.operate(request);
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupMemberRequest request) {
        // Check that the Tenant ID is not null (i.e. this is an existing Tenant)
        Assert.isTrue(!request.isNew(), "When deleting a Tenant, the ID must not be null");
        // Call the Tenants service to delete the Tenant and return the result as a Mono
        return this.groupMembersService.delete(request);
    }

}