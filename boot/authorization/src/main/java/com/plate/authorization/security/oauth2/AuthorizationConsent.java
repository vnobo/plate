package com.plate.authorization.security.oauth2;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Data
@Entity
@Table(name = "se_oauth2_authorization_consent")
@IdClass(AuthorizationConsent.AuthorizationConsentId.class)
public class AuthorizationConsent implements Serializable {
    @Id
    private String registeredClientId;
    @Id
    private String principalName;

    @Column(length = 1000)
    private String authorities;

    @Data
    public static class AuthorizationConsentId implements Serializable {
        private String registeredClientId;
        private String principalName;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AuthorizationConsentId that = (AuthorizationConsentId) o;
            return registeredClientId.equals(that.registeredClientId) && principalName.equals(that.principalName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(registeredClientId, principalName);
        }
    }
}