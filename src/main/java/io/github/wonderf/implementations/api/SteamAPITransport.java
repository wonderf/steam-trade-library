package io.github.wonderf.implementations.api;

import io.github.wonderf.interfaces.factories.HttpClientFactory;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.Set;
import java.util.stream.Collectors;

public class SteamAPITransport {
    private CookieStore cookieStore;
    private final CloseableHttpClient client;
    public SteamAPITransport() {
        this.cookieStore = new BasicCookieStore();
        HttpClientFactory factory = new HttpClientFactory() {
        };
        this.client = factory.createDefault(cookieStore);
    }

    public SteamAPITransport(HttpClientFactory factory) {
        cookieStore = new BasicCookieStore();
        client = factory.createDefault(cookieStore);
    }

    //todo check abilty for override store
    public SteamAPITransport restoreFromCookieStore(BasicCookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    CloseableHttpClient getClient() {
        return this.client;
    }

    public boolean loggedIn() {
        Set<String> cookieNames = cookieStore.getCookies().stream().map(Cookie::getName).collect(Collectors.toUnmodifiableSet());
        return cookieNames.contains("sessionid") && cookieNames.contains("steamLoginSecure");
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

}
