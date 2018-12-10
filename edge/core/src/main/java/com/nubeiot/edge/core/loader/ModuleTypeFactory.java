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
            return ModuleType.JAVASCRIPT;
        }
        if (ModuleType.RUBY.name().equalsIgnoreCase(type)) {
            return ModuleType.RUBY;
        }
        if (ModuleType.GROOVY.name().equalsIgnoreCase(type)) {
            return ModuleType.GROOVY;
        }
        if (ModuleType.SCALA.name().equalsIgnoreCase(type)) {
            return ModuleType.SCALA;
        }
        if (ModuleType.KOTLIN.name().equalsIgnoreCase(type)) {
            return ModuleType.KOTLIN;
        }
        return getDefault();
    }

}
