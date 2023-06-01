package io.github.wonderf.interfaces.encrypt;

import java.security.spec.RSAPublicKeySpec;

public interface PasswordEncryptobale {
    String encrypt(RSAPublicKeySpec pubKeySpec, String pass);
}
