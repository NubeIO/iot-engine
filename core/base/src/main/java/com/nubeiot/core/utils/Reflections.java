package com.nubeiot.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Functions.Silencer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reflections {

    private static final Logger logger = LoggerFactory.getLogger(Reflections.class);

    /**
     * Gets the current thread context class loader.
     *
     * @return the context class loader, may be null
     */
    public static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Gets the class loader of this library.
     *
     * @return the static library class loader, may be null
     */
    public static ClassLoader staticClassLoader() {
        return Reflections.class.getClassLoader();
    }

    /**
     * Returns an array of class loaders initialized from the specified array.
     * <p>
     * If the input is null or empty, it defaults to both {@link #contextClassLoader()} and {@link
     * #staticClassLoader()}
     *
     * @param classLoaders Given class loaders
     * @return the array of class loaders, not null
     */
    public static ClassLoader[] classLoaders(ClassLoader... classLoaders) {
        if (classLoaders != null && classLoaders.length != 0) {
            return classLoaders;
        } else {
            ClassLoader contextClassLoader = contextClassLoader(), staticClassLoader = staticClassLoader();
            return contextClassLoader != null ? staticClassLoader != null && contextClassLoader != staticClassLoader
                                                ? new ClassLoader[] {contextClassLoader, staticClassLoader}
                                                : new ClassLoader[] {contextClassLoader} : new ClassLoader[] {};
        }
    }

    public static <T extends Member> Predicate<T> hasModifiers(int... modifiers) {
        int searchMods = Arrays.stream(modifiers).reduce((left, right) -> left | right).orElse(0);
        return member -> (member.getModifiers() & searchMods) == searchMods;
    }

    public static <T extends Member> Predicate<T> notModifiers(int... modifiers) {
        int searchMods = Arrays.stream(modifiers).reduce((left, right) -> left | right).orElse(0);
        return member -> (member.getModifiers() & searchMods) != searchMods;
    }

    @SafeVarargs
    public static <T extends AnnotatedElement> Predicate<T> hasAnnotation(Class<? extends Annotation>... annotations) {
        return element -> Arrays.stream(annotations).anyMatch(a -> Objects.nonNull(element.getAnnotation(a)));
    }

    public static class ReflectionField {

        public static Predicate<Field> CONSTANT_FILTER = hasModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        /**
         * Find declared fields in given {@code class} that matches with filter
         *
         * @param clazz     Given {@code class} to find methods
         * @param predicate Given predicate
         * @return Stream of matching {@code fields}
         */
        public static Stream<Field> findToStream(@NonNull Class<?> clazz, Predicate<Field> predicate) {
            Stream<Field> stream = Stream.of(clazz.getDeclaredFields());
            if (Objects.nonNull(predicate)) {
                return stream.filter(predicate);
            }
            return stream;
        }

        public static List<Field> find(@NonNull Class<?> clazz, Predicate<Field> predicate) {
            return findToStream(clazz, predicate).collect(Collectors.toList());
        }

        public static <T> T constantByName(@NonNull Class<?> clazz, String name) {
            Predicate<Field> filter = Functions.and(CONSTANT_FILTER,
                                                    f -> f.getName().equals(Strings.requireNotBlank(name)));
            return (T) findToStream(clazz, filter).map(field -> getConstant(clazz, field)).findFirst().orElse(null);
        }

        public static <T> List<T> getConstants(@NonNull Class<?> clazz, @NonNull Class<T> fieldClass,
                                               Predicate<Field> predicate) {
            return streamConstants(clazz, fieldClass, predicate).collect(Collectors.toList());
        }

        public static <T> Stream<T> streamConstants(@NonNull Class<?> clazz, @NonNull Class<T> fieldClass,
                                                    Predicate<Field> predicate) {
            Predicate<Field> filter = Functions.and(CONSTANT_FILTER,
                                                    f -> ReflectionClass.assertDataType(fieldClass, f.getType()));
            if (Objects.nonNull(predicate)) {
                filter = filter.and(predicate);
            }
            return findToStream(clazz, filter).map(field -> getConstant(clazz, field));
        }

        public static <T> T getConstant(@NonNull Class<?> clazz, Field field) {
            try {
                return (T) field.get(null);
            } catch (IllegalAccessException | ClassCastException e) {
                throw new NubeException(
                    Strings.format("Failed to get field constant {0} of {1}", field.getName(), clazz.getName()), e);
            }
        }

        public static <T> T getConstant(@NonNull Class<?> clazz, Field field, T fallback) {
            try {
                return (T) field.get(null);
            } catch (IllegalAccessException | ClassCastException e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed to get field constant {} of {}", e, field.getName(), clazz.getName());
                }
                return fallback;
            }
        }

        public static <T> List<T> getFieldValuesByType(@NonNull Object obj, @NonNull Class<T> searchType) {
            Predicate<Field> predicate = Functions.and(notModifiers(Modifier.STATIC),
                                                       f -> ReflectionClass.assertDataType(f.getType(), searchType));
            return findToStream(obj.getClass(), predicate).map(f -> getFieldValue(obj, f, searchType))
                                                          .filter(Objects::nonNull)
                                                          .collect(Collectors.toList());
        }

        public static <T> T getFieldValue(@NonNull Object obj, @NonNull Field f, @NonNull Class<T> type) {
            try {
                f.setAccessible(true);
                return type.cast(f.get(obj));
            } catch (IllegalAccessException | ClassCastException e) {
                logger.warn("Cannot get data of field {}", e, f.getName());
                return null;
            }
        }

    }


    public static class ReflectionMethod {

        /**
         * Execute method from object instance by only one param.
         *
         * @param <I>        Type of Input parameter
         * @param <O>        Type of Return result
         * @param instance   Object instance
         * @param method     Method to execute
         * @param outputType Output type of {@code method}
         * @param paramType  Param type of {@code method}
         * @param paramData  Argument to pass into {@code method}
         * @return Method result
         * @throws NubeException if any error when invoke method
         */
        public static <I, O> O executeMethod(@NonNull Object instance, @NonNull Method method,
                                             @NonNull Class<O> outputType, @NonNull Class<I> paramType, I paramData) {
            return executeMethod(instance, method, outputType, Collections.singletonList(paramType), paramData);
        }

        public static <O> O executeMethod(@NonNull Object instance, @NonNull Method method,
                                          @NonNull Class<O> outputType, Collection<Class<?>> inputTypes,
                                          Object... inputData) {
            try {
                if (inputTypes.size() != inputData.length) {
                    throw new IllegalArgumentException("Input types does not match with input data");
                }
                if (!validateMethod(method, outputType, inputTypes)) {
                    throw new IllegalArgumentException(
                        "Given method does not match with given output type and input type");
                }
                method.setAccessible(true);
                return (O) method.invoke(instance, inputData);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.debug("Cannot execute method {}", e, method.getName());
                if (e instanceof InvocationTargetException) {
                    Throwable targetException = ((InvocationTargetException) e).getTargetException();
                    if (targetException instanceof NubeException) {
                        throw (NubeException) targetException;
                    }
                    if (Objects.nonNull(targetException)) {
                        throw new NubeException(targetException);
                    }
                }
                throw new NubeException(e);
            }
        }

        /**
         * Find declared methods in given {@code class} that matches with filter
         *
         * @param clazz     Given {@code class} to find methods
         * @param predicate Given predicate
         * @return List of matching {@code methods}
         */
        public static List<Method> find(@NonNull Class<?> clazz, Predicate<Method> predicate) {
            Stream<Method> methods = Stream.of(clazz.getDeclaredMethods());
            if (Objects.nonNull(predicate)) {
                methods = methods.filter(predicate);
            }
            return methods.collect(Collectors.toList());
        }

        /**
         * Check given {@code Method} is matched with given {@code output} class and {@code inputs} class
         *
         * @param method     Given method
         * @param outputType Given output type
         * @param inputTypes Given input type
         * @return {@code true} if matched, otherwise {@code false}
         */
        public static boolean validateMethod(Method method, Class<?> outputType, Class<?>... inputTypes) {
            return validateMethod(method, outputType, Arrays.asList(inputTypes));
        }

        /**
         * Check given {@code Method} is matched with given {@code output} class and {@code inputs} class
         *
         * @param method     Given method
         * @param outputType Given output type
         * @param inputTypes Given input type
         * @return {@code true} if matched, otherwise {@code false}
         */
        public static boolean validateMethod(@NonNull Method method, @NonNull Class<?> outputType,
                                             @NonNull Collection<Class<?>> inputTypes) {
            if (!ReflectionClass.assertDataType(outputType, method.getReturnType())) {
                return false;
            }
            List<Class<?>> inputs = inputTypes.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (inputs.size() != method.getParameterCount()) {
                return false;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < inputs.size(); i++) {
                if (!ReflectionClass.assertDataType(inputs.get(i), parameterTypes[i])) {
                    return false;
                }
            }
            return true;
        }

        @Getter
        @RequiredArgsConstructor
        public static class MethodInfo {

            private final Method method;
            private final Class<?> output;
            private final LinkedHashMap<String, Class<?>> params;

        }

    }


    public static class ReflectionClass {

        /**
         * @param childClass Given child {@code Class}
         * @param superClass Give super {@code Class}
         * @return {@code true} if {@code childClass} is primitive class or class that sub of {@code superClass}
         * @see Class#isAssignableFrom(Class)
         */
        public static boolean assertDataType(@NonNull Class<?> childClass, @NonNull Class<?> superClass) {
            if (childClass.isPrimitive() && superClass.isPrimitive()) {
                return childClass == superClass;
            }
            if (childClass.isPrimitive()) {
                Class<?> superPrimitiveClass = getPrimitiveClass(superClass);
                return childClass == superPrimitiveClass;
            }
            if (superClass.isPrimitive()) {
                Class<?> childPrimitiveClass = getPrimitiveClass(childClass);
                return childPrimitiveClass == superClass;
            }
            return superClass.isAssignableFrom(childClass);
        }

        public static boolean isVertxOrSystemClass(String clazzName) {
            return clazzName.startsWith("java.") || clazzName.startsWith("javax.") || clazzName.startsWith("sun.*") ||
                   clazzName.startsWith("com.sun.") || clazzName.startsWith("io.vertx.core") ||
                   clazzName.startsWith("io.netty.") || clazzName.startsWith("com.fasterxml.jackson");
        }

        public static boolean isJavaLangObject(Class<?> clazz) {
            return clazz.isPrimitive() || clazz.isEnum() || "java.lang".equals(clazz.getPackage().getName());
        }

        private static <T> Class<?> getPrimitiveClass(Class<T> findClazz) {
            try {
                Field t = findClazz.getField("TYPE");
                if (!hasModifiers(Modifier.PUBLIC, Modifier.STATIC).test(t)) {
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

        /**
         * Scan all classes in given package that matches annotation and sub class given parent class.
         *
         * @param <T>             Type of output
         * @param packageName     Given package name
         * @param parentClass     Given parent class. May {@code interface} class, {@code abstract} class or {@code
         *                        null} if none inherited
         * @param annotationClass Given annotation type class {@code @Target(ElementType.TYPE_USE)}
         * @return List of matching class
         */
        @SuppressWarnings("unchecked")
        public static <T> List<Class<T>> find(String packageName, Class<T> parentClass,
                                              @NonNull Class<? extends Annotation> annotationClass) {
            Strings.requireNotBlank(packageName, "Package name cannot be empty");
            ClassGraph graph = new ClassGraph().enableAnnotationInfo()
                                               .ignoreClassVisibility()
                                               .whitelistPackages(packageName);
            if (logger.isTraceEnabled()) {
                graph.verbose();
            }
            try (ScanResult scanResult = graph.scan()) {
                ClassInfoList infoList;
                if (Objects.nonNull(parentClass)) {
                    if (parentClass.isInterface()) {
                        infoList = scanResult.getClassesImplementing(parentClass.getName());
                    } else {
                        infoList = scanResult.getSubclasses(parentClass.getName());
                    }
                } else {
                    infoList = scanResult.getAllClasses();
                }
                return infoList.filter(clazz -> clazz.hasAnnotation(annotationClass.getName()))
                               .loadClasses()
                               .stream()
                               .map(clazz -> (Class<T>) clazz)
                               .collect(Collectors.toList());
            }
        }

        public static <T> Class<T> findClass(String clazz) {
            try {
                return (Class<T>) Class.forName(Strings.requireNotBlank(clazz), true, Reflections.contextClassLoader());
            } catch (ClassNotFoundException | ClassCastException e) {
                logger.debug("Not found class {}", e, clazz);
                return null;
            }
        }

        public static <T> T createObject(String clazz) {
            final Class<Object> aClass = findClass(clazz);
            if (Objects.isNull(aClass)) {
                return null;
            }
            return (T) createObject(aClass);
        }

        public static <T> T createObject(String clazz, @NonNull Map<Class, Object> inputs) {
            final Class<Object> aClass = findClass(clazz);
            if (Objects.isNull(aClass)) {
                return null;
            }
            return (T) createObject(aClass, inputs);
        }

        public static <T> T createObject(Class<T> clazz) {
            return createObject(clazz, new Silencer<>()).get();
        }

        public static <T> T createObject(Class<T> clazz, Map<Class, Object> inputs) {
            return createObject(clazz, inputs, new Silencer<>()).get();
        }

        public static <T> Silencer<T> createObject(Class<T> clazz, Silencer<T> silencer) {
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                silencer.accept(constructor.newInstance(), null);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                silencer.accept(null, new HiddenException(NubeException.ErrorCode.INITIALIZER_ERROR,
                                                          "Cannot init instance of " + clazz.getName(), e));
            }
            return silencer;
        }

        public static <T> Silencer<T> createObject(Class<T> clazz, @NonNull Map<Class, Object> inputs,
                                                   Silencer<T> silencer) {
            if (inputs.size() > 1 && !(inputs instanceof LinkedHashMap)) {
                throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Inputs must be LinkedHashMap");
            }
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor(inputs.keySet().toArray(new Class[] {}));
                constructor.setAccessible(true);
                silencer.accept(constructor.newInstance(inputs.values().toArray(new Object[] {})), null);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                silencer.accept(null, new HiddenException(NubeException.ErrorCode.INITIALIZER_ERROR,
                                                          "Cannot init instance of " + clazz.getName(), e));
            }
            return silencer;
        }

    }

}
