package com.nubeiot.core.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimes {

    private static final Logger logger = LoggerFactory.getLogger(DateTimes.class);

    public static LocalDateTime nowUTC() {
        return fromUTC(Instant.now());
    }

    public static LocalDateTime fromUTC(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static OffsetDateTime now() {
        return from(Instant.now());
    }

    public static OffsetDateTime from(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static Instant parseISO8601(String datetime) {
        try {
            return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(Strings.requireNotBlank(datetime)));
        } catch (DateTimeParseException e) {
            logger.debug("Invalid date :{}", datetime, e);
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Invalid date", e);
        }
    }

}
