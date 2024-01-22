package com.platform.boot.security.core.group;

import com.platform.boot.commons.utils.ContextUtils;
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
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Group> add(@Valid @RequestBody GroupRequest request) {
        Assert.isNull(request.getId(), "When adding a new Group, the ID must be null");
        return this.groupsService.operate(request);
    }

    @PutMapping("modify")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Group> modify(@Valid @RequestBody GroupRequest request) {
        Assert.notNull(request.getId(), "When modifying an existing Group, the ID must not be null");
        return this.groupsService.operate(request);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Void> delete(@Valid @RequestBody GroupRequest request) {
        Assert.notNull(request.getId(), "When deleting a Group, the ID must not be null");
        return this.groupsService.delete(request);
    }

}