package io.github.wonderf.models.responses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class IncomingOffer {
    private final String tradeOfferId;

    private final String steamId;
}
