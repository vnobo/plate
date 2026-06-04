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
 * Group Controller
 * Provides RESTful API endpoints for group management operations
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("groups")
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService groupsService;

    /**
     * Search for groups based on provided criteria
     *
     * @param request  The search criteria for groups
     * @param pageable Pagination information
     * @return Flux of Group objects matching the search criteria
     */
    @GetMapping("search")
    public Flux<Group> search(GroupReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.groupsService.search(request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    /**
     * Get groups with pagination
     *
     * @param request  The search criteria for groups
     * @param pageable Pagination information
     * @return Mono of PagedModel containing Group objects and pagination metadata
     */
    @GetMapping("page")
    public Mono<PagedModel<Group>> page(GroupReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                this.groupsService.page(request.securityCode(securityDetails.getTenantCode()), pageable))
                .map(PagedModel::new);
    }

    /**
     * Add or update a group
     *
     * @param request Group request data with validation
     * @return Mono of Group representing the saved group
     */
    @PostMapping("save")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Group> add(@Valid @RequestBody GroupReq request) {
        return this.groupsService.operate(request);
    }

    /**
     * Delete a group by ID
     *
     * @param request Group request containing the ID of the group to delete
     * @return Mono of Void representing completion of the deletion
     */
    @DeleteMapping("delete")
    @PreAuthorize("hasRole(@contextUtils.RULE_ADMINISTRATORS)")
    public Mono<Void> delete(@Valid @RequestBody GroupReq request) {
        Assert.notNull(request.getId(), "When deleting a Group, the ID must not be null");
        return this.groupsService.delete(request);
    }

}