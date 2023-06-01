package io.github.wonderf.implementations.api;

public abstract class SteamAPI {
    protected final SteamAPITransport apiTransport;

    public SteamAPI(SteamAPITransport transport){
        this.apiTransport=transport;
    }
}
