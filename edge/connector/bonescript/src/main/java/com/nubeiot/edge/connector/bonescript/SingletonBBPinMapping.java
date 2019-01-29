package com.nubeiot.edge.connector.bonescript;

import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;
import com.nubeiot.edge.connector.bonescript.enums.BBVersion;

import lombok.Getter;

public class SingletonBBPinMapping {

    private static BBPinMapping instance;
    @Getter
    private static String version;

    public static BBPinMapping getInstance() {
        if (instance == null) {
            return getInstance(null);
        }
        return instance;
    }

    public static BBPinMapping getInstance(String v) {
        if (instance == null) {
            version = v;
            instance = BBVersion.getBbPinMapping(v);
        }
        return instance;
    }

}
