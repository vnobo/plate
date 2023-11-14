package com.platform.boot.security.core.group.authority;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

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
        return this.authoritiesService.search(request, pageable);
    }

    @GetMapping("page")
    public Mono<Page<GroupAuthority>> page(GroupAuthorityRequest request, Pageable pageable) {
        return this.authoritiesService.page(request, pageable);
    }

    @PostMapping("save")
    public Mono<GroupAuthority> save(@Valid @RequestBody GroupAuthorityRequest request) {
        return this.authoritiesService.operate(request);
    }

    @PostMapping("batch")
    public Mono<Object> batch(@RequestBody GroupAuthorityRequest request) {
        Assert.notNull(request.getAuthorities(), "Authorities param [authorities] cannot be null!");
        return this.authoritiesService.batch(request).thenReturn(Map.of("success", 200,
                "message", "The operation succeeds and takes effect in a few minutes!"));
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody GroupAuthorityRequest request) {
        // Check that the Group ID is not null (i.e. this is an existing Group)
        Assert.isTrue(!request.isNew(), "When deleting a Group, the ID must not be null");
        // Call the Groups service to delete the Group and return the result as a Mono
        return this.authoritiesService.delete(request);
    }
}