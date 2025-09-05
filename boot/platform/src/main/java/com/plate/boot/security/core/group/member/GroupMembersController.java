package com.plate.boot.security.core.group.member;


import com.plate.boot.commons.ProgressEvent;
import com.plate.boot.commons.utils.DatabaseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for handling operations related to group members, including searching, pagination,
 * saving, and deletion.
 * This controller interacts with the {@link GroupMembersService} to process requests and returns
 * reactive types for asynchronous processing.
 */
@RestController
@RequestMapping("/groups/members")
@RequiredArgsConstructor
public class GroupMembersController {

    private final GroupMembersService groupMembersService;

    /**
     * Search for group members based on the provided request criteria and pagination information.
     *
     * @param request  The search criteria for group members, containing filters like user codes and username
     * @param pageable Pagination information for result paging
     * @return A Flux of GroupMemberRes objects matching the search criteria
     */
    @GetMapping("search")
    public Flux<GroupMemberRes> search(GroupMemberReq request, Pageable pageable) {
        return this.groupMembersService.search(request, pageable);
    }

    /**
     * Retrieve group members with pagination support.
     *
     * @param request  The search criteria for group members, containing filters like user codes and username
     * @param pageable Pagination information for result paging
     * @return A Mono containing a PagedModel of GroupMemberRes objects with pagination metadata
     */
    @GetMapping("page")
    public Mono<PagedModel<GroupMemberRes>> page(GroupMemberReq request, Pageable pageable) {
        return this.groupMembersService.page(request, pageable).map(PagedModel::new);
    }

    /**
     * Save or update a group member based on the provided request data.
     *
     * @param request The group member data to save or update, validated before processing
     * @return A Mono containing the saved GroupMember entity
     */
    @PostMapping("save")
    public Mono<GroupMember> save(@Valid @RequestBody GroupMemberReq request) {
        return this.groupMembersService.operate(request);
    }

    /**
     * Endpoint for batch inserting data with progress monitoring via SSE.
     *
     * @param requests Batch insert request containing data and parameters
     * @return Flux of progress updates as Server-Sent Events
     */
    @PostMapping(path = "batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProgressEvent> saveBatch(@Valid @RequestBody Flux<GroupMemberReq> requests) {
        return DatabaseUtils.batchEvent(requests, this::save);
    }

    /**
     * Delete a group member based on the provided request data.
     *
     * @param request The group member deletion request, must contain a valid ID
     * @return A Mono representing completion of the deletion operation
     */
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupMemberReq request) {
        Assert.notNull(request.getId(), "When deleting a Tenant, the ID must not be null");
        return this.groupMembersService.delete(request);
    }

}