package com.nubeiot.core.http;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.http.utils.RestUtils;
import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.Builder;
import lombok.Getter;

/**
 * Make a mapping dynamically between {@code HTTP endpoint} and {@code EventBus}
 */
public interface IEventBusRestApi {

    Logger getLogger();

    void register(EventBus eventBus, Router router);

    default void registerAppControllerHandler(EventBus eventBus, RoutingContext ctx, Metadata metadata) {
        EventMessage msg = EventMessage.success(metadata.getAction(), RestUtils.convertToRequestData(ctx));
        getLogger().info("Receive message from endpoint: {}", msg.toJson().encode());
        eventBus.send(metadata.getAddress(), msg.toJson(), reply -> handleEventReply(metadata, reply).subscribe(
                eventMessage -> handleHttpResponse(ctx, eventMessage), ctx::fail).dispose());
    }

    default Single<EventMessage> handleEventReply(Metadata metadata, AsyncResult<Message<Object>> reply) {
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
        getLogger().info("Receive message from backend: {}", eventMessage.toJson().encode());
        HttpMethod method = ctx.request().method();
        if (eventMessage.isSuccess()) {
            ctx.response().setStatusCode(HttpStatusMapping.success(method).code()).end(eventMessage.getData().encode());
        } else {
            ctx.response()
               .setStatusCode(HttpStatusMapping.error(method, eventMessage.getError().getCode()).code())
               .end(eventMessage.getError().toJson().encode());
        }
    }

    @Getter
    @Builder(builderClassName = "Builder")
    class Metadata {

        private final String address;
        private final EventType action;
        private final String path;
        private final HttpMethod method;
        private final String paramName;
        private final Metadata forward;

        String getPath() {
            if (EventType.GET_LIST == action || EventType.CREATE == action) {
                return path + "s";
            }
            if (EventType.GET_ONE == action || EventType.HALT == action || EventType.UPDATE == action ||
                EventType.REMOVE == action) {
                return path + "/:" + paramName;
            }
            return path;
        }

    }

}
