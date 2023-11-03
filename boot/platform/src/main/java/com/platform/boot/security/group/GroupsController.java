package com.platform.boot.security.group;

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
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService groupsService;

    @GetMapping("search")
    public Flux<Group> search(GroupRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.groupsService.search(request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    @GetMapping("page")
    public Mono<Page<Group>> page(GroupRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                this.groupsService.page(request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    @PostMapping("add")
    public Mono<Group> add(@Valid @RequestBody GroupRequest request) {
        // Check that the Group ID is null (i.e. this is a new Group)
        Assert.isTrue(request.isNew(), "When adding a new Group, the ID must be null");
        // Call the Groups service to add the Group and return the result as a Mono
        return this.groupsService.operate(request);
    }

    @PutMapping("modify")
    public Mono<Group> modify(@Valid @RequestBody GroupRequest request) {
        // Check that the Group ID is not null (i.e. this is an existing Group)
        Assert.isTrue(!request.isNew(), "When modifying an existing Group, the ID must not be null");
        // Call the Groups service to modify the Group and return the result as a Mono
        return this.groupsService.operate(request);
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupRequest request) {
        // Check that the Group ID is not null (i.e. this is an existing Group)
        Assert.isTrue(!request.isNew(), "When deleting a Group, the ID must not be null");
        // Call the Groups service to delete the Group and return the result as a Mono
        return this.groupsService.delete(request);
    }

}