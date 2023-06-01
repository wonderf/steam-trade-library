package io.github.wonderf.interfaces.api;

import io.github.wonderf.models.responses.WaitingConfirmation;

import java.util.List;
import java.util.Optional;

public interface SteamConfirmationAPI {
    List<WaitingConfirmation> get(String steamId, String identitySecret);

    Optional<WaitingConfirmation> findByTradeOfferId(String tradeOfferId, String steamId, String identitySecret);
    String accept(WaitingConfirmation waitingConfirmation,String steamId, String identitySecret);
}
