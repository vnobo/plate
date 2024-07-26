package com.plate.boot.security.core.user;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.sql.Update;
import org.springframework.data.web.PagedModel;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @GetMapping("search")
    public Flux<UserResponse> search(UserRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails ->
                this.usersService.search(request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    @GetMapping("page")
    public Mono<PagedModel<UserResponse>> page(UserRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails ->
                        this.usersService.page(request.securityCode(securityDetails.getTenantCode()), pageable))
                .map(PagedModel::new);
    }

    @PostMapping("add")
    public Mono<UserResponse> add(@Valid @RequestBody UserRequest request) {
        Assert.isNull(request.getId(), "When adding a new user, the ID must be null");
        return this.usersService.add(request).map(user -> BeanUtils.copyProperties(user, UserResponse.class));
    }

    @PutMapping("modify")
    public Mono<UserResponse> modify(@Validated(Update.class) @RequestBody UserRequest request) {
        Assert.notNull(request.getId(), "When modifying an existing user, the ID must not be null");
        return this.usersService.modify(request).map(user -> BeanUtils.copyProperties(user, UserResponse.class));
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@RequestBody UserRequest request) {
        Assert.notNull(request.getId(), "When deleting a user, the ID must not be null");
        return this.usersService.delete(request);
    }
}