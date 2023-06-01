package io.github.wonderf.implementations;

import com.alibaba.fastjson2.JSONObject;
import io.github.wonderf.exceptions.TradeCreationException;
import io.github.wonderf.exceptions.TradeNotFoundException;
import io.github.wonderf.implementations.api.DefaultSteamConfirmationAPI;
import io.github.wonderf.implementations.api.DefaultSteamOfferAPI;
import io.github.wonderf.implementations.api.SteamAPITransport;
import io.github.wonderf.implementations.factories.DefaultLoggedTransportFactory;
import io.github.wonderf.interfaces.SteamTradeClient;
import io.github.wonderf.interfaces.api.SteamConfirmationAPI;
import io.github.wonderf.interfaces.api.SteamOfferAPI;
import io.github.wonderf.interfaces.factories.LoggedTransportFactory;
import io.github.wonderf.models.SteamAccount;
import io.github.wonderf.models.TradeOffer;
import io.github.wonderf.models.responses.AcceptedOffer;
import io.github.wonderf.models.responses.CreatedTradeOffer;
import io.github.wonderf.models.responses.IncomingOffer;
import io.github.wonderf.models.responses.WaitingConfirmation;

import java.util.List;
import java.util.Optional;

public class DefaultSteamTradeClient implements SteamTradeClient {
    private final SteamOfferAPI offerAPI;
    private final SteamConfirmationAPI confirmationAPI;
    private final SteamAccount account;

    public DefaultSteamTradeClient(SteamAccount account) {
        LoggedTransportFactory transportFactory = new DefaultLoggedTransportFactory();
        SteamAPITransport steamAPITransport = transportFactory.loggedIn(account);
        offerAPI = new DefaultSteamOfferAPI(steamAPITransport);
        confirmationAPI = new DefaultSteamConfirmationAPI(steamAPITransport);
        this.account = account;
    }

    @Override
    public AcceptedOffer acceptTrade(String tradeOfferId) throws TradeNotFoundException {
        List<IncomingOffer> incomingOffers = offerAPI.myOffers(account.getSteamId());
        Optional<IncomingOffer> incomingOffer = incomingOffers.stream().filter(x -> x.getTradeOfferId().equals(tradeOfferId)).findFirst();
        if (incomingOffer.isPresent()) {
            AcceptedOffer acceptedOffer = offerAPI.acceptOffer(incomingOffer.get().getTradeOfferId(), incomingOffer.get().getSteamId());
            if(acceptedOffer.getMobileConfirmation() != null && acceptedOffer.getEmailConfirmation()) {
                Optional<WaitingConfirmation> byTradeOfferId = confirmationAPI.findByTradeOfferId(tradeOfferId, account.getSteamId(), account.getIdentitySecret());
                if(byTradeOfferId.isPresent())
                    return new AcceptedOffer(confirmationAPI.accept(byTradeOfferId.get(), account.getSteamId(), account.getIdentitySecret()));
            }
        }
        throw new TradeNotFoundException("Not found trade with id " + tradeOfferId);
    }

    @Override
    public String createTrade(TradeOffer tradeOffer) throws TradeCreationException {

        CreatedTradeOffer offer = offerAPI.createOffer(
                tradeOffer.getReceiverSteamId(),
                tradeOffer.getMessage(),
                JSONObject.toJSONString(tradeOffer.getTrade()));
        if (offer.getError() != null)
            throw new TradeCreationException(offer.getError());
        if (offer.getMobileConfirmation()!= null && offer.getMobileConfirmation()) {
            Optional<WaitingConfirmation> byTradeOfferId = confirmationAPI.findByTradeOfferId(offer.getId(), account.getSteamId(), account.getIdentitySecret());
            byTradeOfferId.ifPresent(waitingConfirmation -> confirmationAPI.accept(waitingConfirmation, account.getSteamId(), account.getIdentitySecret()));
        }
        return offer.getId();
    }
}
