package com.nubeiot.core.event;

import java.util.Objects;
import java.util.function.Consumer;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

//TODO More optimize
//TODO Mark address+action is optional
@RequiredArgsConstructor
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
    private Consumer<ErrorMessage> errorHandler;
    private Consumer<Throwable> errorConsumer;

    public ReplyEventHandler(@NonNull String system, @NonNull EventAction action, @NonNull String address,
                             @NonNull Consumer<EventMessage> redirect, Consumer<Throwable> errorConsumer) {
        this.system = system;
        this.action = action;
        this.address = address;
        this.redirect = redirect;
        this.errorConsumer = errorConsumer;
    }

    public ReplyEventHandler(@NonNull String system, @NonNull String address, @NonNull EventAction action,
                             @NonNull Consumer<EventMessage> redirect, Consumer<ErrorMessage> errorHandler) {
        this.system = system;
        this.action = action;
        this.address = address;
        this.redirect = redirect;
        this.errorHandler = errorHandler;
    }

    @Override
    public void accept(AsyncResult<Message<Object>> reply) {
        handleEventReply(reply).subscribe(this::handleReplySuccess, this::handleReplyError);
    }

    private Single<EventMessage> handleEventReply(AsyncResult<Message<Object>> reply) {
        if (reply.failed()) {
            String msg = Strings.format("No reply from action {0} from \"{1}\"", action, address);
            HiddenException hidden = new HiddenException(ErrorCode.EVENT_ERROR, msg, reply.cause());
            return Single.error(new ServiceException("Service unavailable", hidden));
        }
        return Single.just(EventMessage.from(reply.result().body()));
    }

    private void handleReplySuccess(EventMessage eventMessage) {
        logger.info("{}::Backend eventbus response: {}", system, eventMessage.toJson().encode());
        if (eventMessage.isError() && Objects.nonNull(errorHandler)) {
            errorHandler.accept(eventMessage.getError());
        } else {
            redirect.accept(eventMessage);
        }
    }

    private void handleReplyError(Throwable throwable) {
        logger.error("{}::Backend eventbus response error", throwable, system);
        if (Objects.nonNull(errorConsumer)) {
            errorConsumer.accept(throwable);
        } else if (Objects.nonNull(errorHandler)) {
            errorHandler.accept(ErrorMessage.parse(throwable));
        } else {
            redirect.accept(EventMessage.error(EventAction.RETURN, throwable));
        }
    }

}
