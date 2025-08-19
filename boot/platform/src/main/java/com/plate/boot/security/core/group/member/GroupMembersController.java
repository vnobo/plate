package com.plate.boot.security.core.group.member;


import com.plate.boot.commons.ProgressEvent;
import com.plate.boot.commons.utils.DatabaseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("search")
    public Flux<GroupMemberRes> search(GroupMemberReq request, Pageable pageable) {
        return this.groupMembersService.search(request, pageable);
    }

    @GetMapping("page")
    public Mono<Page<GroupMemberRes>> page(GroupMemberReq request, Pageable pageable) {
        return this.groupMembersService.page(request, pageable);
    }

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

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupMemberReq request) {
        Assert.notNull(request.getId(), "When deleting a Tenant, the ID must not be null");
        return this.groupMembersService.delete(request);
    }

}