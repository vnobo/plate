package com.plate.boot.relational.menus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static com.plate.boot.commons.utils.ContextUtils.RULE_ADMINISTRATORS;

/**
 * Handles HTTP requests related to menu management within an application.
 * This controller serves as an intermediary between the client and the {@link MenusService},
 * facilitating operations such as searching, paging, loading personalized menus, saving, and deleting menu items.
 * It integrates with Spring Security to enforce authorization rules toSql applicable.
 *
 * <p>
 * The endpoints provided include:
 * <ul>
 *   <li>{@code /search} - Retrieves a flux of menus based on search criteria and pagination details.</li>
 *   <li>{@code /page} - Returns a mono wrapping a page of menus according to specified criteria and pagination.</li>
 *   <li>{@code /me} - Loads menus tailored to the currently authenticated user's permissions.</li>
 *   <li>{@code /save} - Saves a new or modifies an existing menu based on the provided request body.</li>
 *   <li>{@code /delete} - Deletes a menu identified by the request parameters.</li>
 * </ul>
 * </p>
 *
 * <p>Usage Note: The controller expects valid {@link MenuReq} bodies for POST and DELETE actions and respects
 * Spring's model validation annotations for input sanitization.</p>
 */
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenusController {
    /**
     * Service for managing menu operations.
     */
    private final MenusService menusService;

    /**
     * Handles HTTP GET requests to search for menus based on the provided request and pageable information.
     * The result is a distinct flux of menus based on their authority.
     *
     * @param request  The menu request containing search criteria.
     * @param pageable The pagination information.
     * @return A Flux of Menu entities matching the search criteria.
     */
    @GetMapping("search")
    public Flux<Menu> search(MenuReq request, Pageable pageable) {
        return this.menusService.search(request, pageable).distinct(Menu::getAuthority);
    }

    /**
     * Handles HTTP GET requests to retrieve a paginated list of menus based on the provided request and pageable information.
     *
     * @param request  The menu request containing search criteria.
     * @param pageable The pagination information.
     * @return A Mono of Page containing Menu entities.
     */
    @GetMapping("page")
    public Mono<PagedModel<Menu>> page(MenuReq request, Pageable pageable) {
        return this.menusService.page(request, pageable).map(PagedModel::new);
    }

    /**
     * Handles HTTP GET requests to load menus tailored to the currently authenticated user's permissions.
     * If the user does not have administrator privileges, their specific rules are applied to the request.
     *
     * @param request The menu request containing search criteria.
     * @return A Flux of Menu entities matching the user's permissions.
     */
    @GetMapping("me")
    public Flux<Menu> load(MenuReq request) {
        return ReactiveSecurityContextHolder.getContext().flatMapMany(securityContext -> {
            Authentication authentication = securityContext.getAuthentication();
            var rules = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (!rules.contains(RULE_ADMINISTRATORS)) {
                if (ObjectUtils.isEmpty(rules)) {
                    request.setRules(Set.of("NONE_AUTHORITY"));
                } else {
                    request.setRules(rules);
                }
            }
            return this.menusService.search(request, Pageable.ofSize(Integer.MAX_VALUE)).distinct(Menu::getAuthority);
        });
    }

    /**
     * Handles HTTP POST requests to save a new or modify an existing menu based on the provided request body.
     * If the request indicates a new menu, it is added; otherwise, it is modified.
     *
     * @param request The menu request containing the details of the menu to be saved.
     * @return A Mono of the saved or modified Menu entity.
     */
    @PostMapping("save")
    public Mono<Menu> save(@Valid @RequestBody MenuReq request) {
        if (request.isNew()) {
            return this.menusService.add(request);
        }
        return this.menusService.modify(request);
    }

    /**
     * Handles HTTP DELETE requests to delete a menu identified by the request parameters.
     * Ensures that the request contains valid ID and code before proceeding with the deletion.
     *
     * @param request The menu request containing the details of the menu to be deleted.
     * @return A Mono indicating completion of the delete operation.
     */
    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody MenuReq request) {
        Assert.isTrue(!request.isNew(), "Delete [ID] cannot be empty!");
        Assert.notNull(request.getCode(), "Delete [CODE] cannot be empty!");
        return this.menusService.delete(request);
    }
}