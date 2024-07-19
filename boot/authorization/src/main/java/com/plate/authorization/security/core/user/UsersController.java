package com.plate.authorization.security.core.user;

import com.plate.authorization.commons.utils.BeanUtils;
import com.plate.authorization.commons.utils.ContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<UserResponse> page(UserRequest request, Pageable pageable) {
        var securityDetails = ContextUtils.securityDetails();
        return this.usersService.page(request, pageable);
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