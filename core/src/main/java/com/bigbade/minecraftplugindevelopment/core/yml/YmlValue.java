package com.bigbade.minecraftplugindevelopment.core.yml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YmlValue {
    @Getter
    private final String key;
    private final String value;

    public StringBuilder getYMLString() {
        return new StringBuilder(key).append(": ").append(value);
    }
}
