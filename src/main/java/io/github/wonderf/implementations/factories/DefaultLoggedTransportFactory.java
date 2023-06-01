package io.github.wonderf.implementations.factories;

import com.alibaba.fastjson2.JSONObject;
import io.github.wonderf.implementations.api.DefaultSteamLoginAPI;
import io.github.wonderf.implementations.api.SteamAPITransport;
import io.github.wonderf.interfaces.api.SteamLoginAPI;
import io.github.wonderf.interfaces.factories.LoggedTransportFactory;
import io.github.wonderf.models.RSAParam;
import io.github.wonderf.models.SteamAccount;

public class DefaultLoggedTransportFactory implements LoggedTransportFactory {
    @Override
    public SteamAPITransport loggedIn(SteamAccount account) {

        SteamAPITransport transport = new SteamAPITransport();
        final SteamLoginAPI loginAPI = new DefaultSteamLoginAPI(transport);
        RSAParam rsaParam = loginAPI.fetchRSAParams(account.getUsername());
        JSONObject authResponse = loginAPI.beginAuth(account.getUsername(), account.getPass(), rsaParam);

        JSONObject updateResponse = loginAPI.updateSessionWithSteamGuard(
                authResponse.getJSONObject("response").getString("steamid"),
                account.getSharedSecret(),
                authResponse.getJSONObject("response").getString("client_id"));

        JSONObject pollLoginStatusResponse = loginAPI.pollLoginStatus(authResponse.getJSONObject("response"));
        JSONObject redirects = loginAPI.finalizeRequest(pollLoginStatusResponse);
        loginAPI.upgradeClientCookie(redirects,authResponse.getJSONObject("response").getString("steamid"));
        return transport;
    }
}
