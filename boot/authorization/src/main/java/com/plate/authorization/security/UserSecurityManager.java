package com.plate.authorization.security;

import com.plate.authorization.security.core.user.User;
import org.springframework.jdbc.core.simple.JdbcClient;
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

    private final JdbcClient jdbcClient;

    public UserSecurityManager(DataSource dataSource, JdbcClient jdbcClient) {
        super(dataSource);
        this.jdbcClient = jdbcClient;
        this.setUsersByUsernameQuery(DEF_USERS_BY_USERNAME_QUERY);
        this.setAuthoritiesByUsernameQuery(DEF_AUTHORITIES_BY_USERNAME_QUERY);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = super.loadUserByUsername(username);
        User localUser = jdbcClient.sql("select * from se_users where username = ?")
                .param(username).query(User.class)
                .optional().orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return SecurityDetails.of(localUser.getCode(), user.getUsername(), localUser.getName(),
                user.getPassword(), localUser.getDisabled(), localUser.getAccountExpired(),
                localUser.getAccountLocked(), localUser.getCredentialsExpired(), user.getAuthorities(),
                Map.of("username", user.getUsername()), "username");
    }
}