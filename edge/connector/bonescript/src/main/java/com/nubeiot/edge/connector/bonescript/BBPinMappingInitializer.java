package com.nubeiot.edge.connector.bonescript;

import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;
import com.nubeiot.edge.connector.bonescript.enums.BBVersion;

import lombok.Getter;

public class BBPinMappingInitializer {

    private static BBPinMapping instance;
    @Getter
    private static String version;

    public BBPinMappingInitializer(String v) {
        instance = BBVersion.getBbPinMapping(version);
        version = v;
    }

    public static BBPinMapping getInstance() {
        return instance;
    }

}
