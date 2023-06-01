package io.github.wonderf.implementations.api;

import io.github.wonderf.interfaces.api.SteamConfirmationAPI;
import io.github.wonderf.models.responses.WaitingConfirmation;
import io.github.wonderf.steam.code.generator.SteamCodeGenerator;
import io.github.wonderf.steam.code.generator.implementation.ConfirmationKey;
import io.github.wonderf.steam.code.generator.implementation.SteamGuardCodeGenerator;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultSteamConfirmationAPI extends SteamAPI implements SteamConfirmationAPI {
    private SteamCodeGenerator codeGenerator = new SteamGuardCodeGenerator();
    public DefaultSteamConfirmationAPI(SteamAPITransport transport) {
        super(transport);
    }

    @Override
    public List<WaitingConfirmation> get(String steamId,String identitySecret) {
        ConfirmationKey confirmationKey = codeGenerator.confirmationKey(identitySecret);
        ClassicHttpRequest confirmationsRequest = ClassicRequestBuilder
                .get("https://steamcommunity.com/mobileconf/conf")
                .addParameter("p", codeGenerator.deviceId(steamId))
                .addParameter("a",steamId)
                .addParameter("k",confirmationKey.getKey())
                .addParameter("t", String.valueOf(confirmationKey.getTime()))
                .addParameter("m","android")
                .addParameter("tag","conf")
                .build();
        try {
            return apiTransport.getClient().execute(confirmationsRequest, response -> {
                final HttpEntity entity = response.getEntity();
                String page = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                List<WaitingConfirmation> confirmations = new ArrayList<>();
                Document doc = Jsoup.parse(page);
                Elements select = doc.select(".mobileconf_list_entry");
                for(Element confHtml: select){
                    WaitingConfirmation confirmation = new WaitingConfirmation(
                            confHtml.attr("data-confid"),
                            confHtml.attr("data-key"),
                            confHtml.attr("data-creator"),
                            confirmationsRequest.toString().split(" ")[1]);
                    confirmations.add(confirmation);
                }
                return confirmations;
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot extract confirmations", e);
        }
    }

    @Override
    public Optional<WaitingConfirmation> findByTradeOfferId(String tradeOfferId, String steamId, String identitySecret) {
        List<WaitingConfirmation> confirmations = this.get(steamId, identitySecret);
        return confirmations.stream()
                .filter(c->c.getOfferId().equals(tradeOfferId))
                .findAny();
    }

    @Override
    public String accept(WaitingConfirmation waitingConfirmation,String steamId,String identitySecret) {
        ConfirmationKey confirmationKey = codeGenerator.confirmationKey(identitySecret);
        ClassicHttpRequest confirmationsRequest = ClassicRequestBuilder
                .get("https://steamcommunity.com/mobileconf/ajaxop")
                .addHeader("Referer", waitingConfirmation.getRefer())
                .addParameter("op","allow")
                .addParameter("p", codeGenerator.deviceId(steamId))
                .addParameter("a",steamId)
                .addParameter("k",confirmationKey.getKey())
                .addParameter("t", String.valueOf(confirmationKey.getTime()))
                .addParameter("m","android")
                .addParameter("tag","conf")
                .addParameter("cid", waitingConfirmation.getId())
                .addParameter("ck", waitingConfirmation.getKey())
                .build();
        try {
            return apiTransport.getClient().execute(confirmationsRequest, response -> {
                final HttpEntity entity = response.getEntity();
                String page = EntityUtils.toString(entity);
                EntityUtils.consume(entity);

                return page;
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot accept confirmation", e);
        }
    }
}
