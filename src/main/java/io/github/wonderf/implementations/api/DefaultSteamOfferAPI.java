package io.github.wonderf.implementations.api;

import com.alibaba.fastjson2.JSONObject;
import io.github.wonderf.exceptions.NotLoggedInException;
import io.github.wonderf.interfaces.api.SteamOfferAPI;
import io.github.wonderf.models.responses.AcceptedOffer;
import io.github.wonderf.models.responses.CreatedTradeOffer;
import io.github.wonderf.models.responses.IncomingOffer;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultSteamOfferAPI extends SteamAPI implements SteamOfferAPI {
    private final Pattern PARTNER_TOKEN=Pattern.compile("StartTradeOffer\\( (\\d+) \\)");
    public DefaultSteamOfferAPI(SteamAPITransport transport) {
        super(transport);
        if(!transport.loggedIn()) throw new NotLoggedInException();
    }
//todo required mobile confirmation
    //todo token is required?? ??
    //todo refere https://steamcommunity.com/tradeoffer/new/?partner=378616477
    //todo partner token from steam id profile StartTradeOffer( 378616477 ) or exception profile is not public
    //todo in body partner is steam id

    //todo response {"tradeofferid":"6072115023","needs_mobile_confirmation":true,"needs_email_confirmation":false,"email_domain":"gmail.com"}
    @Override
    public CreatedTradeOffer createOffer(String steamId, String tradeMessage, String jsonTradeOffer) {
        List<NameValuePair> params = new ArrayList<>();

        String partnerToken = findPartnerToken(steamId);
        String inv = this.loadInventory(partnerToken);
        String sessionid = this.apiTransport.getCookieStore().getCookies().stream()
                .filter(x -> x.getName().equals("sessionid") && x.getDomain().contains("steamcommunity.com"))
                .map(Cookie::getValue).findFirst().get();
        params.add(new BasicNameValuePair("sessionid", sessionid));
        params.add(new BasicNameValuePair("serverid","1"));
        params.add(new BasicNameValuePair("partner",steamId));
        params.add(new BasicNameValuePair("tradeoffermessage",tradeMessage));
        params.add(new BasicNameValuePair("json_tradeoffer",jsonTradeOffer));
        params.add(new BasicNameValuePair("captcha",""));
        params.add(new BasicNameValuePair("trade_offer_create_params","{}"));
        ClassicHttpRequest pollLoginStatusRequest = ClassicRequestBuilder
                .post("https://steamcommunity.com/tradeoffer/new/send")
                .addHeader("Referer","https://steamcommunity.com/tradeoffer/new/send/?partner="+
                        partnerToken)
                .addHeader("Origin","https://steamcommunity.com")
                .setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8))
                .build();
        try {
            return apiTransport.getClient().execute(pollLoginStatusRequest, response -> {
                final HttpEntity entity = response.getEntity();
                String jsonContent = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return JSONObject.parseObject(jsonContent,CreatedTradeOffer.class);
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute pollLoginStatus", e);
        }
    }

    @Override
    public String findPartnerToken(String steamId) {
        ClassicHttpRequest pollLoginStatusRequest = ClassicRequestBuilder
                .get("https://steamcommunity.com/profiles/"+steamId)
                .build();
        try {
            return apiTransport.getClient().execute(pollLoginStatusRequest, response -> {
                final HttpEntity entity = response.getEntity();
                String page = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                Matcher matcher = PARTNER_TOKEN.matcher(page);
                if(matcher.find()){
                    return matcher.group(1);
                }
                return "";
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot extract partner token", e);
        }
    }

    @Override
    public String loadInventory(String partnerToken) {
        ClassicHttpRequest request = ClassicRequestBuilder
                .get("https://steamcommunity.com/tradeoffer/new/")
                .addParameter("partner",partnerToken)
                .build();
        try {
            return apiTransport.getClient().execute(request, response -> {
                final HttpEntity entity = response.getEntity();
                String page = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return page;
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot extract partner token", e);
        }
    }

    @Override
    public List<IncomingOffer> myOffers(String steamId) {
        ClassicHttpRequest request = ClassicRequestBuilder
                .get("https://steamcommunity.com/profiles/"+steamId+"/tradeoffers/")
                .build();
        try {
            return apiTransport.getClient().execute(request, response -> {
                final HttpEntity entity = response.getEntity();
                String page = EntityUtils.toString(entity);
                Document parse = Jsoup.parse(page);
                List<IncomingOffer> offers = new ArrayList<>();
                Elements select = parse.select("div.tradeoffer");
                for(Element t : select){
//                    > div.tradeoffer_items_ctn.active > div.link_overlay
                    String senderId = t.select("a").attr("onclick").replaceAll("\\D", "");
                    IncomingOffer offer = new IncomingOffer(t.select("div.tradeoffer_items_ctn.active > div.link_overlay").attr("onclick").replaceAll("\\D",""),senderId);
                    offers.add(offer);
                }
                EntityUtils.consume(entity);
                return offers;
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot extract partner token", e);
        }
    }

    @Override
    public AcceptedOffer acceptOffer(String tradeOfferId, String senderSteamId) {
        List<NameValuePair> params = new ArrayList<>();
        String sessionid = this.apiTransport.getCookieStore().getCookies().stream()
                .filter(x -> x.getName().equals("sessionid") && x.getDomain().contains("steamcommunity.com"))
                .map(Cookie::getValue).findFirst().get();
        params.add(new BasicNameValuePair("sessionid", sessionid));
        params.add(new BasicNameValuePair("serverid","1"));
        params.add(new BasicNameValuePair("partner",senderSteamId));
        params.add(new BasicNameValuePair("captcha",""));
        params.add(new BasicNameValuePair("tradeofferid",tradeOfferId));
        ClassicHttpRequest pollLoginStatusRequest = ClassicRequestBuilder
                .post("https://steamcommunity.com/tradeoffer/"+tradeOfferId+"/accept")
                .addHeader("Referer","https://steamcommunity.com/tradeoffer/"+tradeOfferId)
                .addHeader("Origin","https://steamcommunity.com")
                .setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8))
                .build();

        try {
            return apiTransport.getClient().execute(pollLoginStatusRequest, response -> {
                final HttpEntity entity = response.getEntity();
                String jsonContent = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return JSONObject.parseObject(jsonContent, AcceptedOffer.class);
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute pollLoginStatus", e);
        }
    }


}
