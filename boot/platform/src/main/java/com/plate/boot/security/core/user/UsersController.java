package com.plate.boot.security.core.user;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
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
 * UsersController handles REST API requests related to user management.
 * It provides endpoints for searching, paginating, adding, modifying, and deleting users.
 * Security measures are enforced through tenant code checks, ensuring operations are authorized per tenant context.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    /**
     * Provides business logic operations for managing users, including search, pagination,
     * addition, modification, deletion, and other user-related functionalities.
     * This service interacts with the database through {@link UsersRepository} and utilizes
     * security mechanisms such as tenant code checks to enforce authorization rules.
     */
    private final UsersService usersService;

    /**
     * Searches for users based on the provided request and pagination details.
     * Ensures that the search operation is performed within the context of the tenant
     * derived from the security details of the requesting user.
     *
     * @param request  A UserReq object encapsulating the search criteria. It also supports
     *                 setting a security code through method chaining.
     * @param pageable Specifies the pagination requirements for the result set, including
     *                 page number, size, sorting, etc.
     * @return A Flux of UserRes objects representing the found users matching the
     * search criteria. The results are asynchronously streamed.
     */
    @GetMapping("search")
    public Flux<UserRes> search(UserReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMapMany(details ->
                this.usersService.search(request.securityCode(details.getTenantCode()), pageable));
    }

    /**
     * Retrieves a paginated list of users based on the provided request and pagination details.
     * Ensures the operation is executed within the context of the authenticated tenant's security code.
     *
     * @param request  A UserReq object encapsulating the user search criteria and additional request-specific fields.
     * @param pageable Specifies the pagination details such as page number, size, sort orders, etc.
     * @return A Mono wrapping a PagedModel of UserRes objects representing the paged user data.
     */
    @GetMapping("page")
    public Mono<Page<UserRes>> page(UserReq request, Pageable pageable) {
        return ContextUtils.securityDetails().flatMap(details ->
                this.usersService.page(request.securityCode(details.getTenantCode()), pageable));
    }

    /**
     * Adds a new user based on the provided UserReq.
     *
     * @param request A valid UserReq object containing the details for the user to be added.
     *                The ID within the request must be null, indicating a new user addition.
     * @return A Mono emitting the UserRes representing the newly added user.
     * The UserRes is a JSON-friendly version of the User entity with the password field excluded for security.
     * @throws IllegalArgumentException If the ID within the request is not null, indicating an attempt to add an existing user.
     */
    @PostMapping("add")
    public Mono<UserRes> add(@Valid @RequestBody UserReq request) {
        Assert.isNull(request.getId(), () -> "When adding a new user, the ID must be null");
        return this.usersService.add(request).map(user -> BeanUtils.copyProperties(user, UserRes.class));
    }

    /**
     * Modifies an existing user based on the provided {@link UserReq}.
     * Validates the request ensuring it is intended for updating (annotated with {@link Update}).
     * Ensures the request contains a non-null ID before proceeding with modification.
     * Upon successful update, maps the modified {@link User} to a {@link UserRes} for the API response.
     *
     * @param request A validated {@link UserReq} containing the details for the user modification,
     *                with a non-null ID of the user to be modified.
     * @return A {@link Mono} emitting the updated user information as a {@link UserRes}.
     * @throws IllegalArgumentException If the request's ID is null, indicating an attempt to modify a non-existent user.
     */
    @PutMapping("modify")
    public Mono<UserRes> modify(@Validated(Update.class) @RequestBody UserReq request) {
        Assert.notNull(request.getId(), () -> "When modifying an existing user, the ID must not be null");
        return this.usersService.modify(request).map(user -> BeanUtils.copyProperties(user, UserRes.class));
    }

    /**
     * Deletes a user based on the provided UserReq.
     * Ensures that the 'id' within the request is not null before proceeding with deletion.
     *
     * @param request A UserReq object containing the necessary details to identify the user for deletion.
     *                Must not be null and must contain a valid 'id'.
     * @return A Mono<Void> indicating the completion of the delete operation.
     * If the operation completes successfully, the Mono will be completed without any value.
     * If the 'id' in the request is null, a NullPointerException will be thrown before the operation begins.
     */
    @DeleteMapping("delete")
    public Mono<Void> delete(@RequestBody UserReq request) {
        Assert.notNull(request.getId(), "When deleting a user, the ID must not be null");
        return this.usersService.delete(request);
    }
}