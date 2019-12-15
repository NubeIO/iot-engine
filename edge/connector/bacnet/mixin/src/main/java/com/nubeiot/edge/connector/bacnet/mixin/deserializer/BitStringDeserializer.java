package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Reflections.ReflectionExecutable;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetMixin;
import com.serotonin.bacnet4j.type.primitive.BitString;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BitStringDeserializer<T extends BitString> implements EncodableDeserializer<T, JsonObject> {

    @NonNull
    private final Class<T> clazz;

    @Override
    public @NonNull Class<T> encodableClass() {
        return clazz;
    }

    @Override
    public @NonNull Class<JsonObject> fromClass() {
        return JsonObject.class;
    }

    @Override
    public T parse(@NonNull JsonObject values) {
        final Optional<T> object = Functions.getIfThrow(() -> ReflectionClass.createObject(encodableClass()));
        return object.map(t -> injectBySetter(values, t)).orElseGet(() -> injectByConstructor(values));
    }

    private T injectByConstructor(@NonNull JsonObject values) {
        final Class<T> clazz = encodableClass();
        final Object[] ps = ReflectionExecutable.streamConstructors(clazz, m -> m.isPublic() &&
                                                                                m.getParameterInfo().length > 0 &&
                                                                                Stream.of(m.getParameterInfo())
                                                                                      .allMatch(this::isBooleanParam))
                                                .findFirst()
                                                .map(m -> Stream.of(m.getParameterInfo())
                                                                .filter(p -> Strings.isNotBlank(p.getName()))
                                                                .map(p -> values.getBoolean(toJsonProp(p.getName())))
                                                                .toArray(Object[]::new))
                                                .orElse(null);
        if (Objects.isNull(ps)) {
            return null;
        }
        final Class[] classes = IntStream.range(0, ps.length).mapToObj(i -> boolean.class).toArray(Class[]::new);
        return Functions.getOrThrow(NubeExceptionConverter::friendly,
                                    () -> ReflectionClass.createObject(clazz, classes, ps));
    }

    private T injectBySetter(@NonNull JsonObject values, T object) {
        Function<Method, Boolean> fun = m -> Functions.getOrDefault(false,
                                                                    () -> values.getBoolean(toJsonProp(m.getName())));
        ReflectionExecutable.streamMethods(encodableClass(), this::isSetter)
                            .forEach(m -> ReflectionMethod.execute(object, m, fun.apply(m)));
        return object;
    }

    private String toJsonProp(String name) {
        return BACnetMixin.standardizeKey(name.startsWith("set") ? name.substring(3) : name);
    }

    private boolean isSetter(MethodInfo m) {
        return m.isPublic() && m.getParameterInfo().length == 1 && m.getName().startsWith("set") &&
               isBooleanParam(m.getParameterInfo()[0]) &&
               ReflectionExecutable.isVoid(m.getTypeDescriptor().getResultType());
    }

    private boolean isBooleanParam(MethodParameterInfo param) {
        return ReflectionExecutable.isBoolean(param.getTypeDescriptor());
    }

}
