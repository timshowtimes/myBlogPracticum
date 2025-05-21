package com.timshowtime.util;

import org.springframework.jdbc.support.KeyHolder;

public interface SqlUtils {

    default Number getKey(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to save post: id was not generated");
        }
        return key;
    }
}
