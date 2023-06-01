package io.github.wonderf.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ItemsData {
    private final List<Item> assets;
    private final List<Object> currency = new ArrayList<>();
    private final Boolean ready = false;
}
