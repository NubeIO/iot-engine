package com.nubeiot.core.exceptions;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.reactivex.exceptions.CompositeException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Convert any {@code throwable} to friendly {@code NubeException}. The converter result will be showed directly to end
 * user, any technical information will be log.
 *
 * @see ErrorMessage
 * @see NubeException
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class NubeExceptionConverter implements Function<Throwable, NubeException> {

    private static final Logger logger = LoggerFactory.getLogger(NubeExceptionConverter.class);
    private final boolean friendly;
    private final String overrideMsg;

    /**
     * Friendly converter for human user
     *
     * @param throwable any exception
     * @return nube exception
     */
    public static NubeException friendly(Throwable throwable) {
        return new NubeExceptionConverter(true, null).apply(throwable);
    }

    /**
     * Friendly converter for human user
     *
     * @param throwable   any exception
     * @param overrideMsg Override message
     * @return nube exception
     */
    public static NubeException friendly(Throwable throwable, String overrideMsg) {
        return new NubeExceptionConverter(true, overrideMsg).apply(throwable);
    }

    /**
     * Raw converter for system process
     *
     * @param throwable any exception
     * @return nube exception
     */
    public static NubeException from(Throwable throwable) {
        return new NubeExceptionConverter(false, null).apply(throwable);
    }

    @Override
    public NubeException apply(@NonNull Throwable throwable) {
        Throwable t = throwable;
        if (t instanceof CompositeException) {
            List<Throwable> exceptions = ((CompositeException) throwable).getExceptions();
            t = exceptions.get(exceptions.size() - 1);
        }
        if (t instanceof NubeException) {
            return overrideMsg(friendly ? convertFriendly((NubeException) t, true) : (NubeException) t);
        }
        if (t.getCause() instanceof NubeException) {
            // Rarely case
            logger.debug("Wrapper Exception: ", t);
            return overrideMsg(
                friendly ? convertFriendly((NubeException) t.getCause(), false) : (NubeException) t.getCause());
        }
        return convertFriendly(new NubeException(ErrorCode.UNKNOWN_ERROR, overrideMsg, t), false);
    }

    private NubeException overrideMsg(NubeException t) {
        if (Strings.isBlank(overrideMsg)) {
            return t;
        }
        return new NubeException(t.getErrorCode(), overrideMsg, t.getCause());
    }

    private NubeException convertFriendly(NubeException t, boolean wrapperIsNube) {
        final Throwable cause = t.getCause();
        final ErrorCode code = t.getErrorCode();
        final String message = originMessage(code, t.getMessage());
        if (Objects.isNull(cause)) {
            return new NubeException(code, message);
        }
        if (cause instanceof NubeException) {
            if (!wrapperIsNube) {
                return (NubeException) cause;
            }
            return new NubeException(t.getErrorCode(), includeCauseMessage((NubeException) cause, message), cause);
        }
        return new NubeException(code, includeCauseMessage(cause, message), cause);
    }

    private String originMessage(ErrorCode code, String message) {
        return Strings.isBlank(message) ? code.toString() : message;
    }

    private String includeCauseMessage(Throwable cause, @NonNull String message) {
        if (Strings.isBlank(cause.getMessage())) {
            return message;
        }
        String mc = cause.getMessage().equals("null") ? cause.toString() : cause.getMessage();
        return Strings.format("{0} | Cause: {1}", message, mc);
    }

    private String includeCauseMessage(NubeException cause, @NonNull String message) {
        if (cause instanceof HiddenException) {
            return message;
        }
        String causeMsg = Objects.isNull(cause.getMessage()) ? "" : cause.getMessage();
        return Strings.format("{0} | Cause: {1} - Error Code: {2}", message, causeMsg, cause.getErrorCode());
    }

}
