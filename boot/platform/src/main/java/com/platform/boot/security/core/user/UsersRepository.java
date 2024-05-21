package com.platform.boot.security.core.user;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">https://github.com/vnobo</a>)
 */
public interface UsersRepository extends R2dbcRepository<User, Long> {
    /**
     * find by user code
     *
     * @param code user code
     * @return user response
     */
    Mono<User> findByCode(String code);

    /**
     * get tenant by username
     *
     * @param username username
     * @return user entity
     */
    Mono<User> findByUsername(String username);

    /**
     * check exists by username
     *
     * @param username username
     * @return exists true or false
     */
    Mono<Boolean> existsByUsernameIgnoreCase(String username);

    /**
     * update password by username
     *
     * @param username    username
     * @param newPassword new password
     * @return result count
     */
    @Modifying
    @Query("update se_users set password=:newPassword where username ilike :username")
    Mono<Long> changePassword(String username, String newPassword);

}