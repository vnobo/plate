package com.plate.boot;

import org.junit.jupiter.api.Test;

import java.security.*;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
class BootApplicationTest {

    @Test
    void contextLoads() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        System.out.println(privateKey);
        System.out.println(publicKey);
    }
}