package com.platform.boot.security.core.user.authority;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/users/authorities")
@RequiredArgsConstructor
public class UserAuthoritiesController {

    private final UserAuthoritiesService authoritiesService;

    @GetMapping("search")
    public Flux<UserAuthority> search(UserAuthorityRequest request) {
        return this.authoritiesService.search(request);
    }

    @PostMapping("add")
    public Mono<UserAuthority> add(@Valid @RequestBody UserAuthorityRequest request) {
        Assert.isTrue(request.isNew(), "新增用户时，[ID]必须为空");
        return this.authoritiesService.operate(request);
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody UserAuthorityRequest request) {
        Assert.isTrue(!request.isNew(), "删除用户时，[ID]不能为空");
        return this.authoritiesService.delete(request);
    }

}