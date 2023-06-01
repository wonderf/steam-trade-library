package io.github.wonderf.interfaces;

import io.github.wonderf.exceptions.TradeCreationException;
import io.github.wonderf.exceptions.TradeNotFoundException;
import io.github.wonderf.models.TradeOffer;
import io.github.wonderf.models.responses.AcceptedOffer;

public interface SteamTradeClient {
    AcceptedOffer acceptTrade(String tradeOfferId) throws TradeNotFoundException;
    String createTrade(TradeOffer tradeOffer) throws TradeCreationException;
}
