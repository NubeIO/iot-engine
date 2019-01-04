package com.nubeiot.core.exceptions;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.reactivex.exceptions.CompositeException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.utils.Strings;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Convert any {@code throwable} to friendly {@code NubeException}. The converter result will be showed directly to end
 * user, any technical information will be log.
 *
 * @see ErrorMessage
 */
@NoArgsConstructor
@AllArgsConstructor
public final class NubeExceptionConverter implements Function<Throwable, NubeException> {

    private static final Logger logger = LoggerFactory.getLogger(NubeExceptionConverter.class);
    private boolean friendly = true;

    @Override
    public NubeException apply(@NonNull Throwable throwable) {
        Throwable t = throwable;
        if (t instanceof CompositeException) {
            List<Throwable> exceptions = ((CompositeException) throwable).getExceptions();
            t = exceptions.get(exceptions.size() - 1);
        }
        if (t instanceof NubeException) {
            return friendly ? convertFriendly((NubeException) t, true) : (NubeException) t;
        }
        if (t.getCause() instanceof NubeException) {
            // Rarely case
            logger.debug("Wrapper Exception: ", t);
            return friendly ? convertFriendly((NubeException) t.getCause(), false) : (NubeException) t.getCause();
        }
        return convertFriendly(new NubeException(NubeException.ErrorCode.UNKNOWN_ERROR, null, t), false);
    }

    private NubeException convertFriendly(NubeException t, boolean wrapperIsNube) {
        final Throwable cause = t.getCause();
        final NubeException.ErrorCode code = t.getErrorCode();
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

    private String originMessage(NubeException.ErrorCode code, String message) {
        return Strings.isBlank(message) ? code.toString() : message;
    }

    private String includeCauseMessage(Throwable cause, @NonNull String message) {
        if (Strings.isBlank(cause.getMessage())) {
            return message;
        }
        return Strings.format("{0} | Cause: {1}", message, cause.getMessage());
    }

    private String includeCauseMessage(NubeException cause, @NonNull String message) {
        if (cause instanceof HiddenException) {
            return message;
        }
        String causeMsg = Objects.isNull(cause.getMessage()) ? "" : cause.getMessage();
        return Strings.format("{0} | Cause: {1} - Error Code: {2}", message, causeMsg, cause.getErrorCode());
    }

}
