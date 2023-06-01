package io.github.wonderf.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.security.spec.RSAPublicKeySpec;
@AllArgsConstructor
@Getter
@Setter
public class RSAParam {
    private final RSAPublicKeySpec pubKeySpec;
    private final long timestamp;
}
