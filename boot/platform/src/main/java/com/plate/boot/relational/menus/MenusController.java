package com.plate.boot.relational.menus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static com.plate.boot.commons.utils.ContextUtils.RULE_ADMINISTRATORS;

/**
 * Handles HTTP requests related to menu management within an application.
 * This controller serves as an intermediary between the client and the {@link MenusService},
 * facilitating operations such as searching, paging, loading personalized menus, saving, and deleting menu items.
 * It integrates with Spring Security to enforce authorization rules where applicable.
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

    private final MenusService menusService;

    @GetMapping("search")
    public Flux<Menu> search(MenuReq request, Pageable pageable) {
        return this.menusService.search(request, pageable).distinct(Menu::getAuthority);
    }

    @GetMapping("page")
    public Mono<Page<Menu>> page(MenuReq request, Pageable pageable) {
        return this.menusService.page(request, pageable);
    }

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

    @PostMapping("save")
    public Mono<Menu> save(@Valid @RequestBody MenuReq request) {
        if (StringUtils.hasLength(request.getCode())) {
            return this.menusService.modify(request);
        }
        return this.menusService.add(request);
    }

    @DeleteMapping("delete")
    public Mono<Void> delete(@Valid @RequestBody MenuReq request) {
        Assert.isTrue(!request.isNew(), "Delete [ID] cannot be empty!");
        Assert.notNull(request.getCode(), "Delete [CODE] cannot be empty!");
        return this.menusService.delete(request);
    }
}