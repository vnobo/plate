package com.plate.boot;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
class BootApplicationTest {

    @Test
    void rsaKeyPairGeneratorTest() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        assertThat(privateKey).isNotNull();
        assertThat(publicKey).isNotNull();
        assertThat(privateKey.getAlgorithm()).isEqualTo("RSA");
        assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");

        String message = "Hello, RSA!";
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] messageBytes = message.getBytes();
        byte[] encryptedMessage = cipher.doFinal(messageBytes);

        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessageBytes = cipher.doFinal(encryptedMessage);
        String decryptedMessage = new String(decryptedMessageBytes);
        assertThat(decryptedMessage).isEqualTo(message);
    }
}