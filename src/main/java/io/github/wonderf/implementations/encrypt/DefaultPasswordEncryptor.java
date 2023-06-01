package io.github.wonderf.implementations.encrypt;

import io.github.wonderf.interfaces.encrypt.PasswordEncryptobale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class DefaultPasswordEncryptor implements PasswordEncryptobale {
    @Override
    public String encrypt(RSAPublicKeySpec pubKeySpec, String pass) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64.getEncoder().encodeToString(cipher.doFinal(pass.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("encrypt pass exception", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("missing alg for encrypt pass", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("wrong public key for encrypt pass", e);
        } catch (BadPaddingException e) {
            throw new RuntimeException("encrypt pass exception", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("encrypt pass exception", e);
        }
    }
}
