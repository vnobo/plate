package com.plate.boot.security.core.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * Represents a response object for user data, extending the base User class.
 * This class provides a JSON-friendly version of the User entity, with specific
 * handling for the password field to ensure it is not exposed in responses.
 *
 * <p><strong>NOTE:</strong> The getPassword method is overridden to be ignored during JSON serialization.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRes extends User {

    /**
     * Data full text search entity sort
     */
    @ReadOnlyProperty
    private Double rank;

    @Override
    public String getPhone() {
        String phone = super.getPhone();
        return phone != null && phone.length() >= 7 ?
                phone.replaceAll("(\\d{3})\\d{4}(\\d*)", "$1****$2") : phone;
    }

    @Override
    public String getEmail() {
        String email = super.getEmail();
        if (email != null && email.contains("@")) {
            String[] parts = email.split("@");
            String username = parts[0];
            String domain = parts[1];
            if (username.length() > 2) {
                username = username.substring(0, 2) + "****";
            }
            return username + "@" + domain;
        }
        return email;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return super.getPassword();
    }

}