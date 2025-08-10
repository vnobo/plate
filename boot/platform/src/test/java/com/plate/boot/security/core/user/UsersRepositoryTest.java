package com.plate.boot.security.core.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UsersRepository Unit Tests
 *
 * <p>This test class provides unit tests for the UsersRepository interface, covering:</p>
 * <ul>
 *   <li>Repository interface contract</li>
 *   <li>Method signatures and return types</li>
 * </ul>
 *
 * @author Qwen Code
 */
class UsersRepositoryTest {

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("Should extend R2dbcRepository")
        void shouldExtendR2dbcRepository() {
            // Given
            Class<?> repositoryClass = UsersRepository.class;

            // When
            boolean isR2dbcRepository = org.springframework.data.r2dbc.repository.R2dbcRepository.class
                    .isAssignableFrom(repositoryClass);

            // Then
            assertThat(isR2dbcRepository).isTrue();
        }

        @Test
        @DisplayName("Should have findByCode method")
        void shouldHaveFindByCodeMethod() {
            // Given
            Class<?> repositoryClass = UsersRepository.class;

            // When
            java.lang.reflect.Method[] methods = repositoryClass.getDeclaredMethods();
            boolean hasFindByCode = java.util.Arrays.stream(methods)
                    .anyMatch(method -> method.getName().equals("findByCode"));

            // Then
            assertThat(hasFindByCode).isTrue();
        }

        @Test
        @DisplayName("Should have findByUsername method")
        void shouldHaveFindByUsernameMethod() {
            // Given
            Class<?> repositoryClass = UsersRepository.class;

            // When
            java.lang.reflect.Method[] methods = repositoryClass.getDeclaredMethods();
            boolean hasFindByUsername = java.util.Arrays.stream(methods)
                    .anyMatch(method -> method.getName().equals("findByUsername"));

            // Then
            assertThat(hasFindByUsername).isTrue();
        }

        @Test
        @DisplayName("Should have existsByUsernameIgnoreCase method")
        void shouldHaveExistsByUsernameIgnoreCaseMethod() {
            // Given
            Class<?> repositoryClass = UsersRepository.class;

            // When
            java.lang.reflect.Method[] methods = repositoryClass.getDeclaredMethods();
            boolean hasExistsByUsernameIgnoreCase = java.util.Arrays.stream(methods)
                    .anyMatch(method -> method.getName().equals("existsByUsernameIgnoreCase"));

            // Then
            assertThat(hasExistsByUsernameIgnoreCase).isTrue();
        }
    }
}