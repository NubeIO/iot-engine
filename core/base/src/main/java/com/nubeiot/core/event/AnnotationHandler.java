package com.nubeiot.core.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Functions.JsonFunction;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Reflections.ReflectionMethod;
import com.nubeiot.core.utils.Reflections.ReflectionMethod.MethodInfo;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class AnnotationHandler<T extends EventHandler> {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandler.class);
    private final T eventHandler;

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
        JsonFunction func = JsonFunction.builder()
                                        .mapper(eventHandler.mapper())
                                        .collectionKey(eventHandler.key())
                                        .build();
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
     */
    private Object[] parseMessage(EventMessage message, Map<String, Class<?>> params) {
        if (params.isEmpty()) {
            return new Object[] {};
        }
        JsonObject data = message.getData();
        if (params.size() == 1) {
            return new Object[] {data.mapTo(params.values().stream().findFirst().orElse(null))};
        }
        return params.entrySet().stream().map(entry -> {
            Object paramData = data.getValue(entry.getKey());
            return Objects.isNull(paramData)
                   ? null
                   : eventHandler.mapper().convertValue(data.getMap(), entry.getValue());
        }).toArray();
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

}
