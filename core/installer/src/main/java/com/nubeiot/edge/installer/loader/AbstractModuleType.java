package com.nubeiot.edge.installer.loader;

import com.nubeiot.core.utils.Reflections.ReflectionField;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
abstract class AbstractModuleType implements ModuleType {

    static <T extends ModuleType> T factory(@NonNull String type, @NonNull Class<T> clazz) {
        return ReflectionField.streamConstants(clazz)
                              .filter(vmt -> vmt.name().equalsIgnoreCase(type))
                              .findFirst()
                              .orElse(null);
    }

    @EqualsAndHashCode.Include
    public abstract String name();

    @Override
    public final String toString() {
        return this.name();
    }


    static abstract class AbstractVertxModuleType extends AbstractModuleType implements VertxModuleType {}


    static abstract class AbstractExecutableBinaryModuleType extends AbstractModuleType
        implements ExecutableBinaryModuleType {}


    static abstract class AbstractExecutableArchiverVertxModuleType extends AbstractModuleType
        implements ExecutableArchiverModuleType {}

}
