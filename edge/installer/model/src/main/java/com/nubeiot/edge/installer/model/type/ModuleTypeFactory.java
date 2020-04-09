package com.nubeiot.edge.installer.model.type;

import java.util.Objects;
import java.util.stream.Stream;

import com.nubeiot.core.utils.Reflections.ReflectionField;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ModuleTypeFactory {

    static <T extends ModuleType> T factory(@NonNull String type, @NonNull Class<T> clazz) {
        return ReflectionField.streamConstants(clazz)
                              .filter(vmt -> vmt.type().equalsIgnoreCase(type))
                              .findFirst()
                              .orElse(null);
    }

    static ModuleType factory(String type) {
        return Stream.of(VertxModuleType.class, ExecutableBinaryModuleType.class,
                         LocalExecutableArchiverModuleType.class, HttpExecutableArchiverModuleType.class)
                     .map(clazz -> ReflectionMethod.executeStatic(clazz, "factory", type))
                     .filter(Objects::nonNull)
                     .map(ModuleType.class::cast)
                     .findFirst()
                     .orElseGet(ModuleType::getDefault);
    }

}
