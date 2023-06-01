package io.github.wonderf.models;

import io.github.wonderf.models.request.JsonTrade;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class TradeOffer {
    private final String receiverSteamId;
    private String message;

    private final JsonTrade trade;
}
