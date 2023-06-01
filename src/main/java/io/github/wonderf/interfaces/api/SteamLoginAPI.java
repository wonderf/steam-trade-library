package io.github.wonderf.interfaces.api;

import com.alibaba.fastjson2.JSONObject;
import io.github.wonderf.models.RSAParam;

public interface SteamLoginAPI {
    JSONObject beginAuth(String username,String pass,RSAParam param);

    RSAParam fetchRSAParams(String accountName);

    JSONObject updateSessionWithSteamGuard(String steamId,String sharedSecret,String clientId);

    JSONObject pollLoginStatus(JSONObject startSession);

    JSONObject finalizeRequest(JSONObject answer);
    void upgradeClientCookie(JSONObject object, String steamId);
}
