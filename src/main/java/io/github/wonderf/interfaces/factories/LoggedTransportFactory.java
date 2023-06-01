package io.github.wonderf.interfaces.factories;

import io.github.wonderf.implementations.api.SteamAPITransport;
import io.github.wonderf.models.SteamAccount;

public interface LoggedTransportFactory {
    SteamAPITransport loggedIn(SteamAccount account);
}
