package com.nubeiot.core.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

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

    public static ZonedDateTime toUTC(@NonNull Date date) {
        return DateTimes.toUTC(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    public static ZonedDateTime toUTC(@NonNull LocalDateTime time) {
        return DateTimes.toUTC(time, ZoneId.systemDefault());
    }

    public static ZonedDateTime toUTC(@NonNull LocalDateTime time, @NonNull ZoneId zoneId) {
        return DateTimes.toUTC(time.atZone(zoneId));
    }

    public static ZonedDateTime toUTC(@NonNull ZonedDateTime dateTime) {
        return DateTimes.toZone(dateTime, ZoneOffset.UTC);
    }

    public static ZonedDateTime toZone(ZonedDateTime dateTime, @NonNull ZoneId toZone) {
        return dateTime.withZoneSameInstant(toZone);
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

    public static ZonedDateTime parseISO8601ToZone(String datetime) {
        return ZonedDateTime.from(parseFromISO8601(datetime));
    }

    public static Instant parseISO8601(String datetime) {
        return Instant.from(parseFromISO8601(datetime));
    }

    public static String format(@NonNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static String format(@NonNull OffsetDateTime offsetDateTime) {
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static JsonObject format(@NonNull Date date) {
        return format(date, null);
    }

    public static JsonObject format(@NonNull Date date, TimeZone timeZone) {
        final ZoneId zoneId = Objects.isNull(timeZone) ? ZoneId.systemDefault() : timeZone.toZoneId();
        final ZonedDateTime zonedDateTime = date.toInstant().atZone(zoneId);
        final ZonedDateTime utcTime = toUTC(zonedDateTime);
        return new JsonObject().put("local", format(zonedDateTime)).put("utc", format(utcTime));
    }

    private static TemporalAccessor parseFromISO8601(String datetime) {
        try {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(Strings.requireNotBlank(datetime));
        } catch (DateTimeParseException e) {
            logger.debug("Invalid date :{}", datetime, e);
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid date", e);
        }
    }

}
