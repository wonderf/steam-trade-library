package io.github.wonderf.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SteamAccount {
    private final String username;
    private final String pass;
    private final String sharedSecret;
    private String steamId;
    private final String identitySecret;
}
