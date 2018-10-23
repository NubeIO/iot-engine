package com.nubeio.iot.share.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reflections {

    private static final Logger logger = LoggerFactory.getLogger(Reflections.class);

    /**
     * Execute method from object instance by only one input data.
     *
     * @param instance    Object instance
     * @param method      Method to execute
     * @param inputData   Argument to pass into {@code method}
     * @param outputClazz Output type of {@code method}
     * @param <I>         Type of Input parameter
     * @param <O>         Type of Return result
     * @return Method's result. {@code null} if any invocation error.
     */
    @SuppressWarnings("unchecked")
    public static <I, O> O executeMethod(@NonNull Object instance, @NonNull Method method, I inputData,
                                         @NonNull Class<O> outputClazz) {
        try {
            final int parameterCount = method.getParameterCount();
            if (parameterCount != 1) {
                throw new IllegalArgumentException("Method '" + method.getName() + "' does not accept one argument");
            }
            final Class<?> parameterType = method.getParameterTypes()[0];
            if (!parameterType.isInstance(inputData)) {
                throw new IllegalArgumentException(
                        "Method '" + method.getName() + "' does not accept " + inputData.getClass().getName() +
                        " as argument");
            }
            final Class<?> returnType = method.getReturnType();
            Class<?> primitiveClass = getPrimitiveClass(outputClazz);
            if (returnType.isPrimitive() ? returnType != primitiveClass : returnType != outputClazz) {
                throw new IllegalArgumentException(
                        "Method '" + method.getName() + "' does not accept " + outputClazz.getName() +
                        " as return type");
            }
            method.setAccessible(true);
            return (O) method.invoke(instance, inputData);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.debug("Cannot execute method {}", e, method.getName());
            return null;
        }
    }

    private static <P> Class<?> getPrimitiveClass(Class<P> findClazz) {
        try {
            Field t = findClazz.getField("TYPE");
            if (!Modifier.isPublic(t.getModifiers()) || !Modifier.isStatic(t.getModifiers())) {
                return null;
            }
            Object primitiveClazz = t.get(null);
            if (primitiveClazz instanceof Class) {
                return (Class<?>) primitiveClazz;
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.trace("Try casting primitive class from class {0}", e, findClazz.getName());
        }
        return null;
    }

}
