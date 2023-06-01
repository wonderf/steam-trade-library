package io.github.wonderf.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Item {
    private final Integer appid;
    private final String contextid="2";//todo what is it
    private final Integer amount;
    private final String assetid;
}
