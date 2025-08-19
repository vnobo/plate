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
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@RestController
@RequestMapping("/groups/authorities")
@RequiredArgsConstructor
public class GroupAuthoritiesController {

    private final GroupAuthoritiesService authoritiesService;

    @GetMapping("search")
    public Flux<GroupAuthority> search(GroupAuthorityReq request, Pageable pageable) {
        return this.authoritiesService.search(request, pageable);
    }

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

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupAuthorityReq request) {
        Assert.notNull(request.getId(), "When deleting a Group, the ID must not be null");
        return this.authoritiesService.delete(request);
    }
}