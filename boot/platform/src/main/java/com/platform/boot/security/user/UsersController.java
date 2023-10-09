package com.platform.boot.security.user;


import com.platform.boot.commons.utils.ContextUtils;
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

    private final UsersServiceAbstract usersService;

    // Endpoint to search for users
    @GetMapping("search")
    public Flux<User> search(UserRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(securityDetails -> this.usersService.search(
                request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    // Endpoint to get a page of users
    @GetMapping("page")
    public Mono<Page<User>> page(UserRequest request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(securityDetails -> this.usersService.page(
                request.securityCode(securityDetails.getTenantCode()), pageable));
    }

    // Endpoint to add a user
    @PostMapping("add")
    public Mono<User> add(@Valid @RequestBody UserRequest request) {
        // Check that the user ID is null (i.e. this is a new user)
        Assert.isTrue(request.isNew(), "When adding a new user, the ID must be null");
        // Call the users service to add the user and return the result as a Mono
        return this.usersService.add(request);
    }

    // Endpoint to modify a user
    @PutMapping("modify")
    public Mono<User> modify(@Validated(Update.class) @RequestBody UserRequest request) {
        // Check that the user ID is not null (i.e. this is an existing user)
        Assert.isTrue(!request.isNew(), "When modifying an existing user, the ID must not be null");
        // Call the users service to modify the user and return the result as a Mono
        return this.usersService.operate(request);
    }

    // Endpoint to delete a user
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody UserRequest request) {
        // Check that the user ID is not null (i.e. this is an existing user)
        Assert.isTrue(!request.isNew(), "When deleting a user, the ID must not be null");
        // Call the users service to delete the user and return the result as a Mono
        return this.usersService.delete(request);
    }
}