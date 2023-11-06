package com.platform.boot.relational.menus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

import static com.platform.boot.commons.utils.ContextUtils.RULE_ADMINISTRATORS;

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

    @GetMapping("search")
    @PreAuthorize("hasRole(T(com.platform.boot.commons.utils.ContextUtils).RULE_ADMINISTRATORS)")
    public Flux<Menu> search(MenuRequest request) {
        return this.menusService.search(request).distinct(Menu::getAuthority);
    }

    @GetMapping("me")
    public Flux<Menu> load(MenuRequest request) {
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
            return this.search(request);
        });
    }

    @PostMapping("save")
    @PreAuthorize("hasRole(T(com.platform.boot.commons.utils.ContextUtils).RULE_ADMINISTRATORS)")
    public Mono<Menu> save(@Valid @RequestBody MenuRequest request) {
        Assert.isTrue(request.isNew(), "This is a message for developers indicating that when " +
                "adding a new menu,the ID field must not have a value," +
                " and if you need to modify an existing menu, the [/modify] endpoint should be used instead.");
        if (StringUtils.hasLength(request.getCode())) {
            return this.menusService.modify(request);
        }
        return this.menusService.add(request);
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasRole(T(com.platform.boot.commons.utils.ContextUtils).RULE_ADMINISTRATORS)")
    public Mono<Void> delete(@Valid @RequestBody MenuRequest request) {
        Assert.isTrue(!request.isNew(), "Delete [ID] cannot be empty!");
        Assert.notNull(request.getCode(), "Delete [CODE] cannot be empty!");
        return this.menusService.delete(request);
    }
}