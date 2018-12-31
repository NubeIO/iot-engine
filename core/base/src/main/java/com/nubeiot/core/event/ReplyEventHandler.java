package com.nubeiot.core.event;

import java.util.Objects;
import java.util.function.Consumer;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.utils.Strings;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class ReplyEventHandler implements Consumer<AsyncResult<Message<Object>>> {

    private static final Logger logger = LoggerFactory.getLogger(ReplyEventHandler.class);
    @NonNull
    private final String system;
    @NonNull
    private final EventAction action;
    @NonNull
    private final String address;
    @NonNull
    private final Consumer<EventMessage> redirect;
    private Consumer<Throwable> errorConsumer;

    @Override
    public void accept(AsyncResult<Message<Object>> reply) {
        handleEventReply(reply).subscribe(this::handleReplySuccess, this::handleReplyError);
    }

    private Single<EventMessage> handleEventReply(AsyncResult<Message<Object>> reply) {
        if (reply.failed()) {
            String msg = Strings.format("No reply from action {0} from \"{1}\"", action, address);
            HiddenException hidden = new HiddenException(NubeException.ErrorCode.EVENT_ERROR, msg, reply.cause());
            return Single.error(new ServiceException("Service unavailable", hidden));
        }
        return Single.just(EventMessage.from(reply.result().body()));
    }

    private void handleReplySuccess(EventMessage eventMessage) {
        logger.info("{}::Backend eventbus response: {}", system, eventMessage.toJson().encode());
        redirect.accept(eventMessage);
    }

    private void handleReplyError(Throwable throwable) {
        logger.error("{}::Backend eventbus response error", system, throwable);
        if (Objects.nonNull(errorConsumer)) {
            errorConsumer.accept(throwable);
        } else {
            redirect.accept(EventMessage.error(EventAction.RETURN, throwable));
        }
    }

}
