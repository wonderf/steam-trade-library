package io.github.wonderf.models.responses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class WaitingConfirmation {
    private final String id;
    private final String key;

    private final String offerId;

    private final String refer;
}
