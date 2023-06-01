[![license](https://img.shields.io/github/license/wonderf/steam-trade-library)](https://github.com/wonderf/steam-trade-library/blob/master/LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/wonderf/steam-trade-library)](https://github.com/wonderf/steam-trade-library/stargazers)
[![user repos](https://badgen.net/github/dependents-repo/wonderf/steam-trade-library?label=user%20repos)](https://github.com/wonderf/steam-trade-library/network/dependents)
[![Visits Badge](https://badges.strrl.dev/visits/wonderf/steam-trade-library)](https://badges.strrl.dev/visits/wonderf/steam-trade-library)
# Java Library for Steam Trades
This library provides minimal features for create and accept Steam Trades. Auth keys and confirmation keys provides by [steam-code-generator](https://github.com/wonderf/steam-code-generator). Don't forget put Stars ⭐⭐⭐ to repository.
If you interested in this library, please awake me by transactions to my USDT wallet bellow.
<br><br>
![Looking for USDT for new projects](/img/USDT.jpg "USDT For new projects" )
# Usage example
## Dependency
```xml

<dependency>
    <groupId>io.github.wonderf</groupId>
    <artifactId>steam-trade-library</artifactId>
    <version>1.0.0</version>
</dependency>
```
## Create Steam trade offer
```java
SteamAccount account = new SteamAccount(username,pass,sharedsecret,steamId,identity);
SteamTradeClient defaultSteamTradeClient = new DefaultSteamTradeClient(account);
JsonTrade tradeItems = new JsonTrade(
        new ItemsData(List.of(new Item(570,1,"17847315800"))),//my items
        new ItemsData(new ArrayList<>())//them items
        );
TradeOffer tradeOffer = new TradeOffer("76561198338880005",tradeItems);//76561198338880005 - receiver steam id
String tradeId = defaultSteamTradeClient.createTrade(tradeOffer);

```

## Accept Steam trade offer
```java
SteamAccount account = new SteamAccount(username,pass,sharedsecret,steamId,identity);
SteamTradeClient defaultSteamTradeClient = new DefaultSteamTradeClient(account);
defaultSteamTradeClient.acceptTrade(tradeId);
```