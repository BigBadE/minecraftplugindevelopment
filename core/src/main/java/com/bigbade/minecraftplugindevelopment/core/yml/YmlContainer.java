package com.bigbade.minecraftplugindevelopment.core.yml;

import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class YmlContainer extends YmlValue {
    private final List<YmlValue> values = new ArrayList<>();
    private int depth = 0;

    public YmlContainer(@Nullable String key) {
        super(key, null);
        if(key == null) {
            depth = -1;
        }
    }

    public YmlContainer addValue(YmlValue value) {
        values.add(value);
        if(value instanceof YmlContainer) {
            ((YmlContainer) value).depth = depth+1;
        }
        return this;
    }

    @Override
    public StringBuilder getYMLString() {
        StringBuilder output = new StringBuilder();
        if(depth > 0) {
            pad(output, depth).append(getKey()).append(":\n");
        }
        for(YmlValue value : values) {
            pad(output, depth+1).append(value.getYMLString()).append("\n");
        }
        return output;
    }

    private static StringBuilder pad(StringBuilder builder, int amount) {
        for(int i = 0; i < amount; i++) {
            builder.append("    ");
        }
        return builder;
    }
}
