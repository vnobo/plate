package com.plate.auth.security;

import com.plate.auth.security.core.user.User;
import com.plate.auth.security.core.user.UsersRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
public class UserSecurityManager extends JdbcUserDetailsManager {
    private static final String DEF_USERS_BY_USERNAME_QUERY = "select username, password," +
            "(case when disabled then false else true end) as enabled " +
            "from se_users where username = ?";
    private static final String DEF_AUTHORITIES_BY_USERNAME_QUERY = "select username,authority "
            + "from se_authorities sa,se_users su "
            + "where username = ? and sa.user_code=su.code";

    private final UsersRepository usersRepository;

    public UserSecurityManager(DataSource dataSource, UsersRepository usersRepository) {
        super(dataSource);
        this.usersRepository = usersRepository;
        this.setUsersByUsernameQuery(DEF_USERS_BY_USERNAME_QUERY);
        this.setAuthoritiesByUsernameQuery(DEF_AUTHORITIES_BY_USERNAME_QUERY);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = super.loadUserByUsername(username);
        User localUser = this.usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        return SecurityDetails.of(localUser.getCode(), user.getUsername(), localUser.getName(),
                user.getPassword(), localUser.getDisabled(), localUser.getAccountExpired(),
                localUser.getAccountLocked(), localUser.getCredentialsExpired(), user.getAuthorities(),
                Map.of("username", user.getUsername()), "username");
    }
}