package com.plate.authorization.security.core.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author <a href="https://github.com/vnobo">AlexBob</a>
 */
public interface UsersRepository extends JpaRepository<User, Long> {
    /**
     * find by user code
     *
     * @param code user code
     * @return user response
     */
    Optional<User> findByCode(String code);

    /**
     * get user by username
     *
     * @param username username
     * @return user entity
     */
    Optional<User> findByUsername(String username);

    /**
     * check exists by username
     *
     * @param username username
     * @return exists true or false
     */
    Boolean existsByUsernameIgnoreCase(String username);

}