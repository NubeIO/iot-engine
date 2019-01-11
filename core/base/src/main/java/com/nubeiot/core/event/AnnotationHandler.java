package com.nubeiot.core.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.JsonData.SerializerFunction;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;
import com.nubeiot.core.utils.Reflections.ReflectionMethod.MethodInfo;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

final class AnnotationHandler<T extends EventHandler> {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandler.class);
    private final T eventHandler;
    private final SerializerFunction func;

    AnnotationHandler(T eventHandler) {
        this.eventHandler = eventHandler;
        this.func = SerializerFunction.builder()
                                      .mapper(eventHandler.mapper())
                                      .backupKey(eventHandler.fallback())
                                      .build();
    }

    Single<JsonObject> execute(@NonNull EventMessage message) {
        if (!eventHandler.getAvailableEvents().contains(message.getAction())) {
            throw new StateException("Unsupported event " + message.getAction());
        }
        MethodInfo methodInfo = getMethodByAnnotation(eventHandler.getClass(), message.getAction());
        Object response = ReflectionMethod.executeMethod(eventHandler, methodInfo.getMethod(), methodInfo.getOutput(),
                                                         methodInfo.getParams().values(),
                                                         parseMessage(message, methodInfo.getParams()));
        return convertResult(response);
    }

    @SuppressWarnings("unchecked")
    private Single<JsonObject> convertResult(Object response) {
        if (response instanceof Single) {
            return ((Single) response).map(func::apply);
        }
        return Single.just(func.apply(response));
    }

    /**
     * Parse event message to many data inputs
     *
     * @param message Given {@link EventMessage}
     * @param params  Given inputClasses
     * @return data inputs
     * @throws NubeException if message format is invalid
     */
    private Object[] parseMessage(EventMessage message, Map<String, Class<?>> params) {
        if (params.isEmpty()) {
            return new Object[] {};
        }
        JsonObject data = message.isError() ? message.getError().toJson() : message.getData();
        if (Objects.isNull(data)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT,
                                    Strings.format("Event Message Data is null: {0}", message.toJson()));
        }
        if (params.size() == 1) {
            return new Object[] {convertParam(data, params.entrySet().iterator().next(), true)};
        }
        return params.entrySet().stream().map(entry -> convertParam(data, entry, false)).toArray();
    }

    static MethodInfo getMethodByAnnotation(@NonNull Class<?> clazz, @NonNull EventAction action) {
        List<Method> methods = ReflectionMethod.find(clazz, filterMethod(action));
        if (methods.isEmpty() || methods.size() > 1) {
            throw new ImplementationError(NubeException.ErrorCode.EVENT_ERROR,
                                          Strings.format("Error when implementing @EventContractor in class {0}",
                                                         clazz.getName()));
        }
        return to(methods.get(0));
    }

    private static MethodInfo to(Method method) {
        EventContractor contractor = method.getAnnotation(EventContractor.class);
        LinkedHashMap<String, Class<?>> inputs = Stream.of(method.getParameters())
                                                       .collect(Collectors.toMap(AnnotationHandler::paramName,
                                                                                 Parameter::getType, throwingMerger(),
                                                                                 LinkedHashMap::new));
        return new MethodInfo(method, contractor.returnType(), inputs);
    }

    private static String paramName(Parameter parameter) {
        Param param = parameter.getAnnotation(Param.class);
        return Objects.nonNull(param) && Strings.isNotBlank(param.value()) ? param.value() : parameter.getName();
    }

    private static Predicate<Method> filterMethod(EventAction action) {
        return Functions.and(Reflections.hasModifiers(Modifier.PUBLIC),
                             Reflections.hasAnnotation(EventContractor.class), method -> {
                EventContractor contractor = method.getAnnotation(EventContractor.class);
                return Stream.of(contractor.action()).anyMatch(eventType -> action == eventType);
            });
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

    private Object convertParam(JsonObject data, Entry<String, Class<?>> next, boolean oneParam) {
        ObjectMapper mapper = eventHandler.mapper();
        String paramName = next.getKey();
        Class<?> paramClass = next.getValue();
        Object d = data.getValue(paramName);
        if (Objects.isNull(d)) {
            return oneParam ? tryParseWithoutParam(data, mapper, paramClass) : null;
        }
        return tryParseFromParamName(mapper, paramClass, d);
    }

    private Object tryParseFromParamName(ObjectMapper mapper, Class<?> paramClass, Object d) {
        try {
            if (ReflectionClass.isJavaLangObject(paramClass)) {
                if (ReflectionClass.assertDataType(d.getClass(), paramClass)) {
                    return d;
                }
                return paramClass.cast(d);
            }
            return mapper.convertValue(d, paramClass);
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Message format is invalid", new HiddenException(e));
        }
    }

    private Object tryParseWithoutParam(JsonObject data, ObjectMapper mapper, Class<?> paramClass) {
        try {
            return mapper.convertValue(data.getMap(), paramClass);
        } catch (IllegalArgumentException e) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Message format is invalid", new HiddenException(e));
        }
    }

}
