package com.nubeiot.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nubeiot.core.exceptions.NubeException;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
     * Returns an array of class Loaders initialized from the specified array.
     * <p>
     * If the input is null or empty, it defaults to both {@link #contextClassLoader()} and {@link
     * #staticClassLoader()}
     *
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

    /**
     * Execute method from object instance by only one input data.
     *
     * @param instance    Object instance
     * @param method      Method to execute
     * @param inputData   Argument to pass into {@code method}
     * @param outputClazz Output type of {@code method}
     * @param <I>         Type of Input parameter
     * @param <O>         Type of Return result
     * @return Method's result.
     * @throws IllegalArgumentException if given input/output doesn't match method's signature
     * @throws NubeException            if any error when invoke method
     */
    @SuppressWarnings("unchecked")
    public static <I, O> O executeMethod(@NonNull Object instance, @NonNull Method method, I inputData,
                                         @NonNull Class<O> outputClazz) throws NubeException {
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
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e).getTargetException();
                if (targetException instanceof NubeException) {
                    throw (NubeException) targetException;
                }
                throw new NubeException(targetException);
            }
            throw new NubeException(e);
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

    /**
     * Scan all classes in given package that matches annotation and sub class given parent class.
     *
     * @param <T>             Type of output
     * @param packageName     Given package name
     * @param annotationClass Given annotation type class {@code @Target(ElementType.TYPE_USE)}
     * @param parentClass     Given parent class. May {@code interface} or {@code normal class}
     * @return List of matching class
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Class<T>> scanClassesInPackage(String packageName,
                                                          @NonNull Class<? extends Annotation> annotationClass,
                                                          Class<T> parentClass) {
        Strings.requireNotBlank(packageName, "Package name cannot be empty");
        final ClassGraph graph = new ClassGraph().enableAnnotationInfo()
                                                 .enableClassInfo()
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
            infoList.filter(clazz -> clazz.hasAnnotation(annotationClass.getName()));
            final List<Class<?>> classes = infoList.loadClasses();
            return classes.stream().map(clazz -> (Class<T>) clazz).collect(Collectors.toList());
        }
    }

    public static <T> T createObject(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.warn("Cannot init instance of {}", e, clazz.getName());
            return null;
        }
    }

}
