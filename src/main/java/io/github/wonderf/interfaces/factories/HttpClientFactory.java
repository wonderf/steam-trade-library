package io.github.wonderf.interfaces.factories;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public interface HttpClientFactory {
    default CloseableHttpClient createDefault(CookieStore cookieStore){
        return HttpClients
                .custom()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                .setDefaultCookieStore(cookieStore)
                .build();
    }
}
