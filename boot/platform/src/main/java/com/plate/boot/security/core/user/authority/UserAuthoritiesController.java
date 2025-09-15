package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.ProgressEvent;
import com.plate.boot.commons.utils.DatabaseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing user authorities.
 * This class provides endpoints for searching, saving, and deleting user authorities.
 * It uses reactive programming with Project Reactor.
 * <p>
 * The class is annotated with \@RestController to indicate that it's a REST controller in the Spring context.
 * It is also annotated with \@RequestMapping to map HTTP requests to handler methods.
 * \@RequiredArgsConstructor is used to generate a constructor with required arguments.
 * <p>
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/users/authorities")
@RequiredArgsConstructor
public class UserAuthoritiesController {

    /**
     * The service for managing user authorities.
     */
    private final UserAuthoritiesService authoritiesService;

    /**
     * Searches for user authorities based on the given request parameters.
     *
     * @param request the user authority request containing search criteria
     * @return a Flux emitting the user authorities that match the search criteria
     */
    @GetMapping("search")
    public Flux<UserAuthority> search(UserAuthorityReq request) {
        return this.authoritiesService.search(request);
    }

    /**
     * Saves a user authority based on the given request.
     * If the user authority exists, it updates the user authority; otherwise, it creates a new user authority.
     *
     * @param request the user authority request containing user authority information
     * @return a Mono emitting the saved user authority
     */
    @PostMapping("save")
    public Mono<UserAuthority> save(@Valid @RequestBody UserAuthorityReq request) {
        return this.authoritiesService.operate(request);
    }

    /**
     * Endpoint for batch inserting data with progress monitoring via SSE.
     *
     * @param requests Batch insert request containing data and parameters
     * @return Flux of progress updates as Server-Sent Events
     */
    @PostMapping(path = "batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProgressEvent> saveBatch(@Valid @RequestBody Flux<UserAuthorityReq> requests) {
        return DatabaseUtils.batchEvent(requests, this::save);
    }

    /**
     * Deletes a user authority based on the given request.
     *
     * @param request the user authority request containing user authority information
     * @return a Mono indicating when the deletion is complete
     */
    @DeleteMapping("delete")
    public Mono<Void> delete(@RequestBody UserAuthorityReq request) {
        Assert.notNull(request.getId(), "When deleting a UserAuthority, the ID must not be null");
        return this.authoritiesService.delete(request);
    }

}