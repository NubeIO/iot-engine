package com.nubeiot.core.event;

import java.util.Objects;
import java.util.function.Consumer;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public final class ReplyEventHandler implements Handler<AsyncResult<Message<Object>>> {

    private static final String REPLY_SYSTEM = "REPLY";
    private static final Logger logger = LoggerFactory.getLogger(ReplyEventHandler.class);
    @NonNull
    @lombok.Builder.Default
    private final String system = REPLY_SYSTEM;
    @NonNull
    private final EventAction action;
    @NonNull
    private final Consumer<EventMessage> success;
    private Consumer<ErrorMessage> error;
    private Consumer<Throwable> exception;

    @Override
    public void handle(AsyncResult<Message<Object>> reply) {
        handleEventReply(reply).subscribe(this::handleReplySuccess, this::handleReplyError);
    }

    private Single<EventMessage> handleEventReply(AsyncResult<Message<Object>> reply) {
        final Message<Object> result = reply.result();
        if (reply.failed()) {
            String msg = Objects.isNull(result)
                         ? Strings.format("No reply from action {0}", action)
                         : Strings.format("No reply from action {0} from \"{1}\"", action, result.address());
            HiddenException hidden = new HiddenException(ErrorCode.EVENT_ERROR, msg, reply.cause());
            return Single.error(new ServiceException("Service unavailable", hidden));
        }
        return Single.just(EventMessage.tryParse(result.body()));
    }

    private void handleReplySuccess(EventMessage eventMessage) {
        logger.info("{}::Backend eventbus response: {}", system, eventMessage.toJson().encode());
        if (eventMessage.isError() && Objects.nonNull(error)) {
            error.accept(eventMessage.getError());
        } else {
            success.accept(eventMessage);
        }
    }

    private void handleReplyError(Throwable throwable) {
        if (Objects.nonNull(exception)) {
            exception.accept(throwable);
        } else if (Objects.nonNull(error)) {
            error.accept(ErrorMessage.parse(throwable));
        } else {
            logger.error("{}::Backend eventbus response error", throwable, system);
            success.accept(EventMessage.error(EventAction.RETURN, throwable));
        }
    }

}
