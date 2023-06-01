package io.github.wonderf.implementations.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.wonderf.implementations.encrypt.DefaultPasswordEncryptor;
import io.github.wonderf.interfaces.api.SteamLoginAPI;
import io.github.wonderf.interfaces.encrypt.PasswordEncryptobale;
import io.github.wonderf.models.RSAParam;
import io.github.wonderf.steam.code.generator.SteamCodeGenerator;
import io.github.wonderf.steam.code.generator.implementation.SteamGuardCodeGenerator;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//todo change to package private
public class DefaultSteamLoginAPI extends SteamAPI implements SteamLoginAPI {
    private final String GET_RSA_KEY = "https://api.steampowered.com/IAuthenticationService/GetPasswordRSAPublicKey/v1/";
    private PasswordEncryptobale encryptobale = new DefaultPasswordEncryptor();

    private SteamCodeGenerator generateOneTimeCode = new SteamGuardCodeGenerator();


    public DefaultSteamLoginAPI(SteamAPITransport transport) {
        super(transport);
    }

    @Override
    public JSONObject beginAuth(String username, String pass, RSAParam param) {
        ClassicHttpRequest beginAuthRequest = ClassicRequestBuilder
                .post("https://api.steampowered.com/IAuthenticationService/BeginAuthSessionViaCredentials/v1/")
                .setEntity(new UrlEncodedFormEntity(prepareLoginRequestData(username, encryptobale.encrypt(param.getPubKeySpec(), pass), param)))
                .build();
        try {
            return apiTransport.getClient().execute(beginAuthRequest, httpResponse -> {
                final HttpEntity entity = httpResponse.getEntity();
                String jsonReponse = EntityUtils.toString(entity);
                EntityUtils.consume(entity);

                //todo migrate to objects


                return JSONObject.parse(jsonReponse);
            });
        } catch (IOException e) {//todo migrate exception
            throw new RuntimeException("Cannot execute begin auth", e);
        }
    }

    @Override
    public RSAParam fetchRSAParams(String accountName) {
        ClassicHttpRequest rsaKeysRequest = ClassicRequestBuilder
                .get(GET_RSA_KEY)
                .addParameter("account_name", accountName)
                .build();
        try {
            return
                    apiTransport.getClient().execute(rsaKeysRequest, response -> {
                        final HttpEntity entity = response.getEntity();

                        String jsonReponse = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);
                        JSONObject jsonObject = JSON.parseObject(jsonReponse);
                        JSONObject responseObject = jsonObject.getJSONObject("response");
                        BigInteger rsaMod = new BigInteger(responseObject.getString("publickey_mod"), 16);
                        BigInteger rsaExp = new BigInteger(responseObject.getString("publickey_exp"), 16);
                        return new RSAParam(new RSAPublicKeySpec(rsaMod, rsaExp), responseObject.getLong("timestamp"));
                    });
        } catch (IOException e) {
            //todo migrate to common exception
            throw new RuntimeException("Cannot execute fetchRSAParams", e);
        }
    }

    @Override
    public JSONObject updateSessionWithSteamGuard(String steamId, String sharedSecret, String clientId) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("steamid", steamId));
        params.add(new BasicNameValuePair("code", generateOneTimeCode.oneTimeCode(sharedSecret)));
        params.add(new BasicNameValuePair("code_type", "3"));

        ClassicHttpRequest updateSessionRequest = ClassicRequestBuilder.post("https://api.steampowered.com/IAuthenticationService/UpdateAuthSessionWithSteamGuardCode/v1/")
                .setEntity(new UrlEncodedFormEntity(params)).build();
        try {
            return apiTransport.getClient().execute(updateSessionRequest, httpResponse -> {
                final HttpEntity entity = httpResponse.getEntity();

                String jsonReponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity);
                return JSONObject.parse(jsonReponse);
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute update session", e);
        }
    }

    @Override
    public JSONObject pollLoginStatus(JSONObject startSession) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", startSession.getString("client_id")));
        params.add(new BasicNameValuePair("request_id", startSession.getString("request_id")));
        ClassicHttpRequest pollLoginStatusRequest = ClassicRequestBuilder
                .post("https://api.steampowered.com/IAuthenticationService/PollAuthSessionStatus/v1/")
                .setEntity(new UrlEncodedFormEntity(params))
                .build();
        try {
            return apiTransport.getClient().execute(pollLoginStatusRequest, response -> {
                final HttpEntity entity = response.getEntity();
                String jsonContent = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return JSONObject.parseObject(jsonContent);
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute pollLoginStatus", e);
        }
    }

    @Override
    public JSONObject finalizeRequest(JSONObject answer) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("nonce", answer.getJSONObject("response").getString("refresh_token")));
        params.add(new BasicNameValuePair("sessionid", getRandomHexString(12)));
        params.add(new BasicNameValuePair("redir", "https://steamcommunity.com/login/home/?goto="));

        ClassicHttpRequest finalizeRequest = ClassicRequestBuilder.post("https://login.steampowered.com/jwt/finalizelogin")
                .setEntity(new UrlEncodedFormEntity(params))
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("sec-fetch-site", "cross-site")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .build();

        try {
            return apiTransport.getClient().execute(finalizeRequest, httpResponse -> {
                final HttpEntity entity = httpResponse.getEntity();
                String jsonResponse = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return JSONObject.parseObject(jsonResponse);
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute finalizeRequest", e);
        }
    }

    @Override
    //todo separate cookie store from inner client or another object for requester
    public void upgradeClientCookie(JSONObject object, String steamId) {
        JSONArray redirects = object.getJSONArray("transfer_info");

        for (int i = 0; i < redirects.size(); i++) {
            JSONObject params = redirects.getJSONObject(i).getJSONObject("params");
            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("nonce", params.getString("nonce")));
            postParameters.add(new BasicNameValuePair("auth", params.getString("auth")));
            postParameters.add(new BasicNameValuePair("steamID", steamId));

            ClassicHttpRequest redirect = ClassicRequestBuilder
                    .post(redirects.getJSONObject(i).getString("url"))
                    .setEntity(new UrlEncodedFormEntity(postParameters, StandardCharsets.UTF_8))
                    .build();
            try {
                apiTransport.getClient().execute(redirect, handler -> null);
            } catch (IOException e) {
                throw new RuntimeException("Cannot execute redirects", e);
            }

        }
    }

    private List<NameValuePair> prepareLoginRequestData(String username, String encPassword, RSAParam param) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("account_name", username));
        params.add(new BasicNameValuePair("encrypted_password", encPassword));
        params.add(new BasicNameValuePair("twofactorcode", ""));
        params.add(new BasicNameValuePair("emailauth", ""));
        params.add(new BasicNameValuePair("loginfriendlyname", ""));
        params.add(new BasicNameValuePair("captchagid", "-1"));
        params.add(new BasicNameValuePair("captcha_text", ""));
        params.add(new BasicNameValuePair("emailsteamid", ""));
        params.add(new BasicNameValuePair("encryption_timestamp", String.valueOf(param.getTimestamp())));
        params.add(new BasicNameValuePair("remember_login", "true"));
        params.add(new BasicNameValuePair("donotcache", String.valueOf(System.currentTimeMillis())));
        return params;
    }

    private String getRandomHexString(int numchars) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }
}
