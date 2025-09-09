package com.plate.boot.security.core.group.authority;

import com.plate.boot.commons.ProgressEvent;
import com.plate.boot.commons.utils.DatabaseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Group Authority Management Controller
 * <p>
 * Provides CRUD operations and batch operations for group authorities with reactive API interfaces.
 * Group authorities are used to manage permission assignments for user groups and are an important 
 * part of the security framework.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@RestController
@RequestMapping("/groups/authorities")
@RequiredArgsConstructor
public class GroupAuthoritiesController {

    private final GroupAuthoritiesService authoritiesService;

    /**
     * Search for group authorities based on specified criteria
     *
     * @param request  Search criteria request object, containing filters such as group code and authority
     * @param pageable Pagination parameters, including page number and page size information
     * @return Flux of group authority information matching the specified criteria
     */
    @GetMapping("search")
    public Flux<GroupAuthority> search(GroupAuthorityReq request, Pageable pageable) {
        return this.authoritiesService.search(request, pageable);
    }

    /**
     * Save group authority information
     * <p>
     * Creates a new record if the specified group authority does not exist, or updates the record if it already exists.
     *
     * @param request Group authority request object containing group code and authority information
     * @return Saved group authority information
     */
    @PostMapping("save")
    public Mono<GroupAuthority> save(@Valid @RequestBody GroupAuthorityReq request) {
        return this.authoritiesService.operate(request);
    }

    /**
     * Endpoint for batch inserting data with progress monitoring via SSE.
     *
     * @param requests Batch insert request containing data and parameters
     * @return Flux of progress updates as Server-Sent Events
     */
    @PostMapping(path = "batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProgressEvent> saveBatch(@Valid @RequestBody Flux<GroupAuthorityReq> requests) {
        return DatabaseUtils.batchEvent(requests, this::save);
    }

    /**
     * Delete the specified group authority
     *
     * @param request Group authority request object which must contain the ID of the record to delete
     * @return Empty result indicating successful deletion
     * @throws IllegalArgumentException Throws an exception when the request object ID is null
     */
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupAuthorityReq request) {
        Assert.notNull(request.getId(), "When deleting a Group, the ID must not be null");
        return this.authoritiesService.delete(request);
    }
}