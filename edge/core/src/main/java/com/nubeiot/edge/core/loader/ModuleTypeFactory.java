package com.nubeiot.edge.core.loader;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleTypeFactory {

    public static ModuleType getDefault() {
        return ModuleType.JAVA;
    }

    public static ModuleType factory(String type) {
        if (ModuleType.JAVASCRIPT.name().equalsIgnoreCase(type)) {
            return ModuleType.JAVA;
        }
        if (ModuleType.JAVA.name().equalsIgnoreCase(type)) {
            return ModuleType.JAVA;
        }
        return getDefault();
    }

}
