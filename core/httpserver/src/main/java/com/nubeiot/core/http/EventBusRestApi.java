package com.nubeiot.core.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.http.handler.JsonContextHandler;
import com.nubeiot.core.http.utils.RestUtils;
import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Make a mapping dynamically between {@code HTTP endpoint} and {@code EventBus}
 */
public interface EventBusRestApi {

    static Map<EventType, HttpMethod> defaultEventHttpMap() {
        Map<EventType, HttpMethod> map = new HashMap<>();
        map.put(EventType.CREATE, HttpMethod.POST);
        map.put(EventType.UPDATE, HttpMethod.PUT);
        map.put(EventType.PATCH, HttpMethod.PATCH);
        map.put(EventType.REMOVE, HttpMethod.DELETE);
        map.put(EventType.GET_ONE, HttpMethod.GET);
        map.put(EventType.GET_LIST, HttpMethod.GET);
        return map;
    }

    List<EventBusRestMetadata> getRestMetadata();

    void register(EventBus eventBus, Router router);

    default void registerAppControllerHandler(EventBus eventBus, RoutingContext ctx, EventBusRestMetadata metadata) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        EventMessage msg = EventMessage.success(metadata.getAction(), RestUtils.convertToRequestData(ctx));
        logger.info("Receive message from endpoint: {}", msg.toJson().encode());
        eventBus.send(metadata.getAddress(), msg.toJson(), reply -> handleEventReply(metadata, reply).subscribe(
                eventMessage -> handleHttpResponse(ctx, eventMessage), ctx::fail));
    }

    default Single<EventMessage> handleEventReply(EventBusRestMetadata metadata, AsyncResult<Message<Object>> reply) {
        if (reply.failed()) {
            NubeException hidden = new HiddenException(NubeException.ErrorCode.EVENT_ERROR,
                                                       Strings.format("No reply from address: {0} - Action: {1}",
                                                                      metadata.getAddress(), metadata.getAction()),
                                                       reply.cause());
            return Single.error(new ServiceException("Service unavailable", hidden));
        }
        EventMessage replyMsg = EventMessage.from(reply.result().body());
        return Single.just(replyMsg);
    }

    default void handleHttpResponse(RoutingContext ctx, EventMessage eventMessage) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Receive message from backend: {}", eventMessage.toJson().encode());
        ctx.put(JsonContextHandler.EVENT_RESULT, eventMessage);
        ctx.next();
    }

}
