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
 * @author <a href="https://github.com/vnobo">Alex bob</a>
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