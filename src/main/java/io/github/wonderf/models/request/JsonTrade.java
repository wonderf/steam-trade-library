package io.github.wonderf.models.request;

import io.github.wonderf.models.ItemsData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class JsonTrade {
    private final Boolean newversion = true;
    private final Integer version = 2;

    private final ItemsData me;
    private final ItemsData them;
}
