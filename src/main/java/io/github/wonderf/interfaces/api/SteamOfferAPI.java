package io.github.wonderf.interfaces.api;

import io.github.wonderf.models.responses.AcceptedOffer;
import io.github.wonderf.models.responses.CreatedTradeOffer;
import io.github.wonderf.models.responses.IncomingOffer;

import java.util.List;

public interface SteamOfferAPI {
    CreatedTradeOffer createOffer(String steamId, String tradeMessage, String jsonTradeOffer);

    String findPartnerToken(String steamId);

    String loadInventory(String partnerToken);

    List<IncomingOffer> myOffers(String steamId);

    AcceptedOffer acceptOffer(String tradeOfferId, String senderSteamId);
}
