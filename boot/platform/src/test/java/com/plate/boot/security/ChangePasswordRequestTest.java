package com.plate.boot.security;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SecurityController.ChangePasswordRequest} DTO.
 * Validates the constraints and behavior of the password change request.
 */
class ChangePasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should validate successfully with valid password and new password")
    void testValidChangePasswordRequest() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("currentPassword123");
        request.setNewPassword("newPassword123");

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when password is blank")
    void testBlankPassword() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("");
        request.setNewPassword("newPassword123");

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password not empty!");
    }

    @Test
    @DisplayName("Should fail validation when new password is blank")
    void testBlankNewPassword() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("currentPassword123");
        request.setNewPassword("");

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("New password not empty!");
    }

    @Test
    @DisplayName("Should fail validation when both passwords are blank")
    void testBothPasswordsBlank() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("");
        request.setNewPassword("");

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(2);
    }

    @Test
    @DisplayName("Should fail validation when password is null")
    void testNullPassword() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword(null);
        request.setNewPassword("newPassword123");

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password not empty!");
    }

    @Test
    @DisplayName("Should fail validation when new password is null")
    void testNullNewPassword() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("currentPassword123");
        request.setNewPassword(null);

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("New password not empty!");
    }

    @Test
    @DisplayName("Should pass validation with minimum valid passwords")
    void testMinimumValidPasswords() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("a");
        request.setNewPassword("b");

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle special characters in passwords")
    void testSpecialCharactersInPasswords() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("current@Password#123!");
        request.setNewPassword("new@Password#456!");

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle very long passwords")
    void testLongPasswords() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("a".repeat(1000));
        request.setNewPassword("b".repeat(1000));

        Set<ConstraintViolation<SecurityController.ChangePasswordRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}