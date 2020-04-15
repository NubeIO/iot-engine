package com.nubeiot.core.kafka.handler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import io.github.zero.utils.Strings;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaHeader;

import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

import lombok.NonNull;

/**
 * @see Headers
 * @see KafkaHeader
 */
public final class KafkaHeaderConverter {

    private static final Logger logger = LoggerFactory.getLogger(KafkaHeaderConverter.class);
    private static final String ERROR_CODE = "nubeio.error.code";
    private static final String ERROR_MESSAGE = "nubeio.error.message";
    private static final String STATUS = "nubeio.status";
    private static final String PREV_ACTION = "nubeio.prevAction";
    private static final String ACTION = "nubeio.action";

    public static List<KafkaHeader> convert(@NonNull EventMessage message) {
        List<KafkaHeader> headers = new ArrayList<>();
        headers.add(KafkaHeader.header(ACTION, message.getAction().name()));
        headers.add(KafkaHeader.header(STATUS, message.getStatus().name()));
        ErrorMessage error = message.getError();
        if (message.isError()) {
            headers.add(KafkaHeader.header(ERROR_CODE, error.getCode().name()));
            headers.add(KafkaHeader.header(ERROR_MESSAGE, error.getMessage()));
        }
        return headers;
    }

    public static List<KafkaHeader> convert(Map<String, Object> headerMaps) {
        List<KafkaHeader> headers = new ArrayList<>();
        if (Objects.nonNull(headerMaps)) {
            headerMaps.forEach((key, value) -> headers.add(
                KafkaHeader.header(key, Buffer.buffer(Strings.toString(value), StandardCharsets.UTF_8.name()))));
        }
        return headers;
    }

    public static EventMessage convert(@NonNull Headers headers) {
        EventAction action = getHeader(headers, ACTION, EventAction.UNKNOWN);
        EventAction prevAction = getHeader(headers, PREV_ACTION, EventAction.UNKNOWN);
        Status status = getHeader(headers, STATUS, Status.INITIAL);
        ErrorMessage error = status == Status.FAILED ? getHeader(headers) : null;
        return Objects.isNull(error)
               ? EventMessage.from(status, action, prevAction)
               : EventMessage.error(action, prevAction, error);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum> T getHeader(@NonNull Headers headers, @NonNull String key, @NonNull T fallback) {
        try {
            return (T) Enum.valueOf(fallback.getClass(), getHeader(headers, key));
        } catch (IllegalArgumentException e) {
            logger.trace("Return fallback value after failed in converting enum class" + fallback.getClass(), e);
            return fallback;
        }
    }

    private static ErrorMessage getHeader(@NonNull Headers headers) {
        ErrorCode code = getHeader(headers, ERROR_CODE, ErrorCode.UNKNOWN_ERROR);
        String message = getHeader(headers, ERROR_MESSAGE);
        return ErrorMessage.parse(code, message);
    }

    private static String getHeader(@NonNull Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (Objects.isNull(header) || Objects.isNull(header.value())) {
            return "";
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

}
