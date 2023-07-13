package com.platform.boot.relational.menus;

import com.platform.boot.commons.utils.ContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller for the Menus resource that handles all the incoming requests for Menus
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenusController {

    private final MenusService menusService;

    /**
     * Endpoint to search Menus
     *
     * @param request MenuRequest with relevant details
     * @return Flux of Menu
     */
    @GetMapping("search")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATORS')")
    public Flux<Menu> search(MenuRequest request) {
        return this.menusService.search(request);
    }

    /**
     * Endpoint to load menus for current user
     *
     * @return Flux of Menu
     */
    @GetMapping("me")
    public Flux<Menu> loadMeMenus() {
        return ContextHolder.securityDetails().flatMapMany(userDetails -> {
            MenuRequest request = MenuRequest.of(userDetails.getTenantCode(), null);
            if (userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_ADMINISTRATORS"
                    .equals(grantedAuthority.getAuthority()))) {
                return this.search(MenuRequest.of(null, null));
            }
            return this.search(request).filter(menu -> userDetails.getAuthorities()
                    .stream().anyMatch(authority -> authority.getAuthority().equals(menu.getAuthority())));
        });
    }

    /**
     * Endpoint to add menu
     *
     * @param request MenuRequest with relevant details
     * @return Mono of Menu
     */
    @PostMapping("add")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATORS')")
    public Mono<Menu> add(@Valid @RequestBody MenuRequest request) {
        Assert.isTrue(request.isNew(), "This is a message for developers indicating that when " +
                "adding a new menu,the ID field must not have a value," +
                " and if you need to modify an existing menu, the [/modify] endpoint should be used instead.");
        return this.menusService.add(request);
    }

    /**
     * Endpoint to modify existing menu
     *
     * @param request MenuRequest with relevant details
     * @return Mono of Menu
     */
    @PutMapping("modify")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATORS')")
    public Mono<Menu> modify(@Valid @RequestBody MenuRequest request) {
        Assert.isTrue(!request.isNew(), "Modify [ID] cannot be empty!");
        Assert.notNull(request.getCode(), "Modify [CODE] cannot be empty!");
        return this.menusService.modify(request);
    }

    /**
     * Endpoint to delete menu
     *
     * @param request MenuRequest with relevant details
     * @return Mono of void
     */
    @DeleteMapping("delete")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATORS')")
    public Mono<Void> delete(@Valid @RequestBody MenuRequest request) {
        Assert.isTrue(!request.isNew(), "Delete [ID] cannot be empty!");
        Assert.notNull(request.getCode(), "Delete [CODE] cannot be empty!");
        return this.menusService.delete(request);
    }
}