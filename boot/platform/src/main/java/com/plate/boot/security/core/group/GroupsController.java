package com.plate.boot.security.core.group;

import com.plate.boot.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("groups")
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService groupsService;

    @GetMapping("search")
    public Flux<Group> search(GroupReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.groupsService.search(request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    @GetMapping("page")
    public Mono<PagedModel<Group>> page(GroupReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                this.groupsService.page(request.securityCode(securityDetails.getTenantCode()), pageable))
                .map(PagedModel::new);
    }

    @PostMapping("save")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Group> add(@Valid @RequestBody GroupReq request) {
        return this.groupsService.operate(request);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Void> delete(@Valid @RequestBody GroupReq request) {
        Assert.notNull(request.getId(), "When deleting a Group, the ID must not be null");
        return this.groupsService.delete(request);
    }

}