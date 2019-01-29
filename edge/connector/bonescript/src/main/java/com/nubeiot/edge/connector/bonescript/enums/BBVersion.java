package com.nubeiot.edge.connector.bonescript.enums;

import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMappingV15;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BBVersion {
    V14("v14", new BBPinMappingV15()), V15("v15", new BBPinMappingV15());

    private final String name;
    private final BBPinMapping bbPinMapping;

    public static BBPinMapping getBbPinMapping(String version) {
        if (version != null) {
            for (BBVersion bbVersion : BBVersion.values()) {
                if (bbVersion.name.equals(version)) {
                    return bbVersion.bbPinMapping;
                }
            }
        }
        return new BBPinMappingV15(); // default one
    }
}
