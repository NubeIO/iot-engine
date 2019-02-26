package com.nubeiot.core.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.NubeException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimes {

    private static final Logger logger = LoggerFactory.getLogger(DateTimes.class);

    public static LocalDateTime nowUTC() {
        return fromUTC(Instant.now());
    }

    public static LocalDateTime fromUTC(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static LocalDateTime toUTC(LocalDateTime time) {
        return DateTimes.toUTC(time, ZoneId.systemDefault());
    }

    public static LocalDateTime toUTC(LocalDateTime time, @NonNull ZoneId fromZone) {
        return DateTimes.toZone(time, fromZone, ZoneOffset.UTC);
    }

    public static LocalDateTime toZone(LocalDateTime time, @NonNull ZoneId fromZone, @NonNull ZoneId toZone) {
        if (Objects.isNull(time)) {
            return null;
        }
        ZonedDateTime zonedTime = time.atZone(fromZone);
        ZonedDateTime converted = zonedTime.withZoneSameInstant(toZone);
        return converted.toLocalDateTime();
    }

    public static OffsetDateTime now() {
        return from(Instant.now());
    }

    public static long nowMilli() {
        return Instant.now().toEpochMilli();
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
