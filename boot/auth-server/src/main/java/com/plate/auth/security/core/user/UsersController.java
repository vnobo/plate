package com.plate.auth.security.core.user;

import com.plate.auth.commons.utils.BeanUtils;
import com.plate.auth.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @GetMapping("search")
    public List<UserResponse> search(UserRequest request, Pageable pageable) {
        var securityDetails = ContextUtils.securityDetails();
        return this.usersService.search(request, pageable);
    }

    @GetMapping("page")
    public PagedModel<UserResponse> page(UserRequest request, Pageable pageable) {
        var securityDetails = ContextUtils.securityDetails();
        var page= this.usersService.page(request, pageable);
        return new PagedModel<>(page);
    }

    @PostMapping("add")
    public UserResponse add(@Valid @RequestBody UserRequest request) {
        Assert.isNull(request.getId(), "When adding a new user, the ID must be null");
        User user = this.usersService.add(request);
        return BeanUtils.copyProperties(user, UserResponse.class);
    }

    @PutMapping("modify")
    public UserResponse modify(@Validated(Update.class) @RequestBody UserRequest request) {
        Assert.notNull(request.getId(), "When modifying an existing user, the ID must not be null");
        User user = this.usersService.modify(request);
        return BeanUtils.copyProperties(user, UserResponse.class);
    }

    @DeleteMapping("delete")
    public void delete(@RequestBody UserRequest request) {
        Assert.notNull(request.getId(), "When deleting a user, the ID must not be null");
        this.usersService.delete(request);
    }
}