package com.plate.authorization.core;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Data
@Entity
@Table(name = "oauth2_authorization_consent")
public class AuthorizationConsent implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String registeredClientId;
    private String principalName;
    @Column(length = 1000)
    private String authorities;

}