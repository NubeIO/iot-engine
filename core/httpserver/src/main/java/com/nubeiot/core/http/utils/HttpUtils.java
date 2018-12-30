package com.nubeiot.core.http.utils;

import java.util.HashMap;
import java.util.Map;

import com.nubeiot.core.http.InvalidUrlException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils {

    private static final String EQUAL = "=";
    private static final String SEPARATE = "&";

    public static Map<String, Object> deserializeQuery(String query) {
        Map<String, Object> map = new HashMap<>();
        for (String property : query.split("\\" + SEPARATE)) {
            String[] keyValues = property.split("\\" + EQUAL);
            if (keyValues.length != 2) {
                throw new InvalidUrlException("Property doesn't conform the syntax: `key`" + EQUAL + "`value`");
            }
            map.put(Urls.decode(keyValues[0]), Urls.decode(keyValues[1]));
        }
        return map;
    }

}
