package io.github.zero.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeSignature;
import io.github.zero.exceptions.HiddenException;
import io.github.zero.exceptions.ReflectionException;
import io.github.zero.utils.Functions.Silencer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reflections {

    private static final Logger LOGGER = LoggerFactory.getLogger(Reflections.class);

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

        public final static Predicate<Field> CONSTANT_FILTER = hasModifiers(Modifier.PUBLIC, Modifier.STATIC,
                                                                            Modifier.FINAL);

        /**
         * Find declared fields in given {@code class} that matches with filter
         *
         * @param clazz     Given {@code class} to find methods
         * @param predicate Given predicate
         * @return Stream of matching {@code fields}
         */
        public static Stream<Field> stream(@NonNull Class<?> clazz, Predicate<Field> predicate) {
            Stream<Field> stream = Stream.of(clazz.getDeclaredFields());
            if (Objects.nonNull(predicate)) {
                return stream.filter(predicate);
            }
            return stream;
        }

        public static List<Field> find(@NonNull Class<?> clazz, Predicate<Field> predicate) {
            return stream(clazz, predicate).collect(Collectors.toList());
        }

        public static <T> T constantByName(@NonNull Class<?> clazz, String name) {
            Predicate<Field> filter = Functions.and(CONSTANT_FILTER,
                                                    f -> f.getName().equals(Strings.requireNotBlank(name)));
            return (T) stream(clazz, filter).map(field -> getConstant(clazz, field)).findFirst().orElse(null);
        }

        public static <T> List<T> getConstants(@NonNull Class<?> clazz, @NonNull Class<T> fieldClass) {
            return streamConstants(clazz, fieldClass).collect(Collectors.toList());
        }

        public static <T> List<T> getConstants(@NonNull Class<?> clazz, @NonNull Class<T> fieldClass,
                                               Predicate<Field> predicate) {
            return streamConstants(clazz, fieldClass, predicate).collect(Collectors.toList());
        }

        public static <T> Stream<T> streamConstants(@NonNull Class<T> clazz) {
            return streamConstants(clazz, clazz, null);
        }

        public static <T> Stream<T> streamConstants(@NonNull Class<?> clazz, @NonNull Class<T> fieldClass) {
            return streamConstants(clazz, fieldClass, null);
        }

        public static <T> Stream<T> streamConstants(@NonNull Class<?> clazz, @NonNull Class<T> fieldClass,
                                                    Predicate<Field> predicate) {
            Predicate<Field> filter = Functions.and(CONSTANT_FILTER,
                                                    f -> ReflectionClass.assertDataType(fieldClass, f.getType()));
            if (Objects.nonNull(predicate)) {
                filter = filter.and(predicate);
            }
            return stream(clazz, filter).map(field -> getConstant(clazz, field));
        }

        public static <T> T getConstant(@NonNull Class<?> clazz, Field field) {
            try {
                return (T) field.get(null);
            } catch (IllegalAccessException | ClassCastException e) {
                throw new ReflectionException(
                    Strings.format("Failed to get field constant {0} of {1}", field.getName(), clazz.getName()), e);
            }
        }

        public static <T> T getConstant(@NonNull Class<?> clazz, Field field, T fallback) {
            try {
                return (T) field.get(null);
            } catch (IllegalAccessException | ClassCastException e) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Failed to get field constant " + field.getName() + " of " + clazz.getName(), e);
                }
                return fallback;
            }
        }

        public static <T> List<T> getFieldValuesByType(@NonNull Object obj, @NonNull Class<T> searchType) {
            Predicate<Field> predicate = Functions.and(notModifiers(Modifier.STATIC),
                                                       f -> ReflectionClass.assertDataType(f.getType(), searchType));
            return stream(obj.getClass(), predicate).map(f -> getFieldValue(obj, f, searchType))
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList());
        }

        public static <T> T getFieldValue(@NonNull Object obj, @NonNull Field f, @NonNull Class<T> type) {
            try {
                f.setAccessible(true);
                return type.cast(f.get(obj));
            } catch (IllegalAccessException | ClassCastException e) {
                LOGGER.warn("Cannot get data of field " + f.getName(), e);
                return null;
            }
        }

    }


    /**
     * @see Executable
     */
    public static class ReflectionExecutable {

        private static <T> Stream<T> scan(@NonNull Class<?> clazz,
                                          @NonNull Function<ClassInfo, Stream<T>> scanFunction) {
            ClassGraph graph = new ClassGraph().enableAnnotationInfo()
                                               .ignoreClassVisibility()
                                               .ignoreMethodVisibility()
                                               .whitelistClasses(clazz.getName());
            if (LOGGER.isTraceEnabled()) {
                graph.verbose();
            }
            ScanResult scanResult = graph.scan();
            final ClassInfo classInfo = scanResult.getClassInfo(clazz.getName());
            if (Objects.isNull(classInfo)) {
                return Stream.<T>empty().onClose(scanResult::close);
            }
            return scanFunction.apply(classInfo).onClose(scanResult::close);
        }

        public static List<Method> streamMethods(@NonNull Class<?> clazz, @NonNull Predicate<MethodInfo> predicate) {
            try (Stream<Method> stream = scan(clazz, classInfo -> classInfo.getMethodInfo()
                                                                           .filter(predicate::test)
                                                                           .stream()
                                                                           .map(MethodInfo::loadClassAndGetMethod))) {
                return stream.collect(Collectors.toList());
            }
        }

        public static Stream<MethodInfo> streamConstructors(@NonNull Class<?> clazz,
                                                            @NonNull Predicate<MethodInfo> predicate) {
            return scan(clazz, classInfo -> classInfo.getConstructorInfo().filter(predicate::test).stream());
        }

        public static boolean isBoolean(TypeSignature signature) {
            return signature instanceof BaseTypeSignature &&
                   ((BaseTypeSignature) signature).getType().equals(boolean.class);
        }

        public static boolean isVoid(TypeSignature signature) {
            return signature instanceof BaseTypeSignature &&
                   ((BaseTypeSignature) signature).getType().equals(void.class);
        }

        public static ReflectionException handleError(@NonNull Executable executable,
                                                      @NonNull ReflectiveOperationException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cannot execute method " + executable.getName(), e);
            }
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e).getTargetException();
                if (targetException instanceof ReflectionException) {
                    throw (ReflectionException) targetException;
                }
                if (Objects.nonNull(targetException)) {
                    throw new ReflectionException(targetException);
                }
            }
            throw new ReflectionException(e);
        }

    }


    public static class ReflectionMethod {

        public static <T> T executeStatic(@NonNull Class<T> clazz, @NonNull String methodName, Object... args) {
            final Predicate<Method> predicate = m -> m.getReturnType().equals(clazz) && m.getName().equals(methodName);
            return (T) find(predicate, clazz).findFirst().map(method -> execute(null, method, args)).orElse(null);
        }

        public static Object execute(@NonNull Object instance, @NonNull Method method) {
            try {
                method.setAccessible(true);
                return method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw ReflectionExecutable.handleError(method, e);
            }
        }

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
         * @throws ReflectionException if any error when invoke method
         */
        public static <I, O> O execute(@NonNull Object instance, @NonNull Method method, @NonNull Class<O> outputType,
                                       @NonNull Class<I> paramType, I paramData) {
            return execute(instance, method, outputType, Collections.singletonList(paramType), paramData);
        }

        public static <O> O execute(@NonNull Object instance, @NonNull Method method, @NonNull Class<O> outputType,
                                    Collection<Class<?>> inputTypes, Object... inputData) {
            if (inputTypes.size() != inputData.length) {
                throw new IllegalArgumentException("Input types does not match with input data");
            }
            if (!validateMethod(method, outputType, inputTypes)) {
                throw new IllegalArgumentException("Given method does not match with given output type and input type");
            }
            return execute(instance, method, inputData);
        }

        public static <O> O execute(Object instance, @NonNull Method method, Object... args) {
            try {
                method.setAccessible(true);
                return (O) method.invoke(instance, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw ReflectionExecutable.handleError(method, e);
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
            return find(Optional.ofNullable(predicate).orElse(method -> true), clazz).collect(Collectors.toList());
        }

        public static Stream<Method> find(@NonNull Predicate<Method> predicate, @NonNull Class<?> clazz) {
            return Stream.of(clazz.getDeclaredMethods()).filter(predicate);
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

        public static boolean isSystemClass(String clazzName) {
            return belongsTo(clazzName, "java.", "javax.", "sun.*", "com.sun.");
        }

        public static boolean belongsTo(@NonNull String clazzName, String... packageNames) {
            return Arrays.stream(packageNames).anyMatch(clazzName::startsWith);
        }

        public static boolean isJavaLangObject(@NonNull Class<?> clazz) {
            return clazz.isPrimitive() || clazz.isEnum() || "java.lang".equals(clazz.getPackage().getName());
        }

        private static <T> Class<?> getPrimitiveClass(@NonNull Class<T> findClazz) {
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
                LOGGER.trace("Try casting primitive class from class " + findClazz.getName(), e);
            }
            return null;
        }

        public static <T> Stream<Class<T>> stream(String packageName, Class<T> parentClass) {
            return stream(packageName, parentClass, clazz -> true);
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
        public static <T> Stream<Class<T>> stream(String packageName, Class<T> parentClass,
                                                  @NonNull Class<? extends Annotation> annotationClass) {
            return stream(packageName, parentClass, clazz -> clazz.hasAnnotation(annotationClass.getName()));
        }

        public static <T> Stream<Class<T>> stream(String packageName, Class<T> parentClass,
                                                  @NonNull Predicate<ClassInfo> filter) {
            Strings.requireNotBlank(packageName, "Package name cannot be empty");
            ClassGraph graph = new ClassGraph().enableAnnotationInfo()
                                               .ignoreClassVisibility()
                                               .whitelistPackages(packageName);
            if (LOGGER.isTraceEnabled()) {
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
                return infoList.filter(filter::test).loadClasses().stream().map(clazz -> (Class<T>) clazz);
            }
        }

        @NonNull
        public static Predicate<ClassInfo> publicClass() {
            return clazz -> clazz.isStandardClass() && clazz.isPublic() && !clazz.isAbstract();
        }

        public static <T> Class<T> findClass(String clazz) {
            try {
                return (Class<T>) Class.forName(Strings.requireNotBlank(clazz), true, Reflections.contextClassLoader());
            } catch (ClassNotFoundException | ClassCastException e) {
                LOGGER.debug("Not found class " + clazz, e);
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
                final Constructor<T> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                silencer.accept(constructor.newInstance(), null);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                silencer.accept(null, new HiddenException(
                    new ReflectionException("Cannot init instance of " + clazz.getName(), e)));
            }
            return silencer;
        }

        public static <T> Silencer<T> createObject(@NonNull Class<T> clazz, @NonNull Map<Class, Object> inputs,
                                                   @NonNull Silencer<T> silencer) {
            if (inputs.size() > 1 && !(inputs instanceof LinkedHashMap)) {
                throw new ReflectionException("Inputs must be LinkedHashMap");
            }
            try {
                final Class[] classes = inputs.keySet().toArray(new Class[] {});
                final Object[] args = inputs.values().toArray(new Object[] {});
                silencer.accept(createObject(clazz, classes, args), null);
            } catch (ReflectiveOperationException e) {
                silencer.accept(null, new HiddenException(
                    new ReflectionException("Cannot init instance of " + clazz.getName(), e)));
            }
            return silencer;
        }

        public static <T> T createObject(@NonNull Class<T> clazz, Class[] classes, Object[] args)
            throws ReflectiveOperationException {
            final Constructor<T> constructor = clazz.getDeclaredConstructor(classes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        }

    }

}
