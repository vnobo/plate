package com.platform.boot.security.core.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.sql.Update;
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
        return this.usersService.search(request, pageable);
    }

    @GetMapping("page")
    public Mono<Page<UserResponse>> page(UserRequest request, Pageable pageable) {
        return this.usersService.page(request, pageable);
    }

    @PostMapping("add")
    public Mono<User> add(@Valid @RequestBody UserRequest request) {
        // Check that the user ID is null (i.e. this is a new user)
        Assert.isTrue(request.isNew(), "When adding a new user, the ID must be null");
        // Call the users service to add the user and return the result as a Mono
        return this.usersService.add(request);
    }

    @PutMapping("modify")
    public Mono<User> modify(@Validated(Update.class) @RequestBody UserRequest request) {
        // Check that the user ID is not null (i.e. this is an existing user)
        Assert.isTrue(!request.isNew(), "When modifying an existing user, the ID must not be null");
        // Call the users service to modify the user and return the result as a Mono
        return this.usersService.operate(request);
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody UserRequest request) {
        // Check that the user ID is not null (i.e. this is an existing user)
        Assert.isTrue(!request.isNew(), "When deleting a user, the ID must not be null");
        // Call the users service to delete the user and return the result as a Mono
        return this.usersService.delete(request);
    }
}