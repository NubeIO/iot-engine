package com.nubeiot.core.http.handler;

import java.util.function.Consumer;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.http.RestEventMetadata;
import com.nubeiot.core.http.utils.RequestConverter;
import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestEventResultHandler implements Handler<RoutingContext> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestEventMetadata metadata;

    @Override
    public void handle(RoutingContext context) {
        EventMessage msg = EventMessage.success(metadata.getAction(), RequestConverter.convert(context));
        logger.info("Receive message from endpoint: {}", msg.toJson().encode());
        new EventController(context.vertx()).request(metadata.getAddress(), metadata.getPattern(), msg,
                                                     new RestEventConsumer(context));
    }

    @RequiredArgsConstructor
    class RestEventConsumer implements Consumer<AsyncResult<io.vertx.core.eventbus.Message<Object>>> {

        private final RoutingContext context;

        @Override
        public void accept(AsyncResult<io.vertx.core.eventbus.Message<Object>> reply) {
            handleEventReply(reply).subscribe(message -> handleHttpResponse(context, message), context::fail);
        }

        private Single<EventMessage> handleEventReply(AsyncResult<Message<Object>> reply) {
            if (reply.failed()) {
                String msg = Strings.format("No reply action {0} from \"{1}\"", metadata.getAction(),
                                            metadata.getAddress());
                HiddenException hidden = new HiddenException(NubeException.ErrorCode.EVENT_ERROR, msg, reply.cause());
                return Single.error(new ServiceException("Service unavailable", hidden));
            }
            return Single.just(EventMessage.from(reply.result().body()));
        }

        private void handleHttpResponse(RoutingContext ctx, EventMessage eventMessage) {
            logger.info("Receive message from backend: {}", eventMessage.toJson().encode());
            ctx.put(EventAction.RETURN.name(), eventMessage);
            ctx.next();
        }

    }

}
