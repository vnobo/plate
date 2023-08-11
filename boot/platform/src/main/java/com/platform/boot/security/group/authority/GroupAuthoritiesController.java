package com.platform.boot.security.group.authority;

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
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@RestController
@RequestMapping("/groups/authorities")
@RequiredArgsConstructor
public class GroupAuthoritiesController {

    private final GroupAuthoritiesService authoritiesService;

    @GetMapping("search")
    public Flux<GroupAuthority> search(GroupAuthorityRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.authoritiesService.search(request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    @GetMapping("page")
    public Mono<Page<GroupAuthority>> page(GroupAuthorityRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                this.authoritiesService.page(request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    // Endpoint to add a Group
    @PostMapping("add")
    public Mono<GroupAuthority> add(@Valid @RequestBody GroupAuthorityRequest request) {
        // Check that the Group ID is null (i.e. this is a new Group)
        Assert.isTrue(request.isNew(), "When adding a new Group, the ID must be null");
        // Call the Groups service to add the Group and return the result as a Mono
        return this.authoritiesService.operate(request);
    }

    // Endpoint to modify a Group
    @PutMapping("modify")
    public Mono<GroupAuthority> modify(@Valid @RequestBody GroupAuthorityRequest request) {
        // Check that the Group ID is not null (i.e. this is an existing Group)
        Assert.isTrue(!request.isNew(), "When modifying an existing Group, the ID must not be null");
        // Call the Groups service to modify the Group and return the result as a Mono
        return this.authoritiesService.operate(request);
    }

    // Endpoint to delete a Group
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupAuthorityRequest request) {
        // Check that the Group ID is not null (i.e. this is an existing Group)
        Assert.isTrue(!request.isNew(), "When deleting a Group, the ID must not be null");
        // Call the Groups service to delete the Group and return the result as a Mono
        return this.authoritiesService.delete(request);
    }
}