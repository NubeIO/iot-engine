package com.nubeiot.core.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.StateException;
import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.NonNull;

/**
 * Handlers a received {@code Eventbus} message.
 *
 * @see EventContractor
 * @see EventMessage
 * @see EventAction
 */
public interface EventHandler extends Consumer<Message<Object>> {

    /**
     * Available events that this handler can process
     *
     * @return list of possible events
     */
    @NonNull List<EventAction> getAvailableEvents();

    @Override
    default void accept(Message<Object> message) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        EventMessage msg = EventMessage.from(message.body());
        logger.info("Executing action: {} with data: {}", msg.getAction(), msg.toJson().encode());
        try {
            handleEvent(msg.getAction(), msg.getData().mapTo(RequestData.class)).subscribe(
                    data -> message.reply(EventMessage.success(msg.getAction(), data).toJson()), throwable -> {
                        logger.error("Failed when handle event", throwable);
                        message.reply(EventMessage.error(msg.getAction(), throwable).toJson());
                    });
        } catch (IllegalArgumentException t) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Data Message format is not correct",
                                    new HiddenException(t));
        } catch (NubeException ex) {
            logger.error("Failed when handle event", ex);
            message.reply(EventMessage.error(msg.getAction(), ex).toJson());
        }
    }

    @SuppressWarnings("unchecked")
    default void accept(io.vertx.reactivex.core.eventbus.Message<Object> message) {
        this.accept(message.getDelegate());
    }

    @SuppressWarnings("unchecked")
    default Single<JsonObject> handleEvent(@NonNull EventAction action, @NonNull RequestData data)
            throws NubeException {
        if (!this.getAvailableEvents().contains(action)) {
            throw new StateException("Unsupported event " + action);
        }
        Method method = getMethodByAnnotation(this.getClass(), action);
        EventContractor contractor = method.getAnnotation(EventContractor.class);
        Class<?> outputClazz = contractor.returnType();
        Object response = Reflections.executeMethod(this, method, data, outputClazz);
        if (response instanceof Single) {
            return ((Single) response).map(o -> o instanceof JsonObject ? o : JsonUtils.toJson(o));
        }
        return Single.just(JsonUtils.toJson(response));
    }

    static Method getMethodByAnnotation(@NonNull Class<?> clazz, @NonNull EventAction action) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            EventContractor contractor = method.getAnnotation(EventContractor.class);
            if (Objects.isNull(contractor)) {
                continue;
            }
            if (Stream.of(contractor.events()).anyMatch(eventType -> action == eventType)) {
                if (method.getParameterCount() != 1 ||
                    !Reflections.assertDataType(method.getParameterTypes()[0], RequestData.class)) {
                    continue;
                }
                if (Reflections.assertDataType(method.getReturnType(), contractor.returnType())) {
                    return method;
                }
            }
        }
        HiddenException.ImplementationError t = new HiddenException.ImplementationError(
                NubeException.ErrorCode.EVENT_ERROR,
                Strings.format("Error when implementing @EventContractor in class {0}", clazz.getName()));
        throw new NubeException(NubeException.ErrorCode.UNKNOWN_ERROR,
                                Strings.format("No reply from event {0}", action), t);
    }

}
