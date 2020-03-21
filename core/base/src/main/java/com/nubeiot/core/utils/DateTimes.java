package com.nubeiot.core.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
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

    public static LocalDateTime fromUTC(@NonNull Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static ZonedDateTime toUTC(@NonNull Date date) {
        return DateTimes.toUTC(date.toInstant());
    }

    public static ZonedDateTime toUTC(@NonNull Instant date) {
        return DateTimes.toUTC(date.atZone(ZoneId.systemDefault()));
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

    public static ZonedDateTime toZone(@NonNull ZonedDateTime dateTime, @NonNull ZoneId toZone) {
        return dateTime.withZoneSameInstant(toZone);
    }

    public static OffsetDateTime now() {
        return from(Instant.now());
    }

    public static long nowMilli() {
        return Instant.now().toEpochMilli();
    }

    public static OffsetDateTime from(@NonNull Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static OffsetDateTime from(long milliseconds) {
        return from(Instant.ofEpochMilli(milliseconds));
    }

    /**
     * Utilities class for parsing {@code date/time/datetime} in {@code iso8601} to appropriate {@code java data type}
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO_8601</a>
     */
    public static class Iso8601Parser {

        public static Instant parse(@NonNull String datetime) {
            return Instant.from(parseFromISO8601(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }

        public static ZonedDateTime parseZonedDateTime(@NonNull String datetime) {
            return ZonedDateTime.from(parseFromISO8601(datetime, DateTimeFormatter.ISO_ZONED_DATE_TIME));
        }

        public static OffsetDateTime parseDateTime(@NonNull String datetime) {
            return OffsetDateTime.from(parseFromISO8601(datetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }

        public static OffsetDateTime parseDate(@NonNull String date) {
            return OffsetDateTime.from(parseFromISO8601(date, DateTimeFormatter.ISO_OFFSET_DATE));
        }

        public static OffsetTime parseTime(@NonNull String time) {
            return OffsetTime.from(parseFromISO8601(time, DateTimeFormatter.ISO_TIME));
        }

        private static TemporalAccessor parseFromISO8601(String datetime, @NonNull DateTimeFormatter formatter) {
            try {
                return formatter.parse(Strings.requireNotBlank(datetime));
            } catch (DateTimeParseException e) {
                logger.debug("Invalid date :{}", datetime, e);
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid date", e);
            }
        }

    }


    /**
     * Utilities class for formatting {@code date/time/datetime} in {@code java data type} to {@code iso8601}
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO_8601</a>
     */
    public static class Iso8601Formatter {

        public static String formatDate(@NonNull ZonedDateTime zonedDate) {
            return zonedDate.format(DateTimeFormatter.ISO_OFFSET_DATE);
        }

        public static String formatDate(@NonNull OffsetDateTime offsetDate) {
            return offsetDate.format(DateTimeFormatter.ISO_OFFSET_DATE);
        }

        public static String formatTime(@NonNull OffsetTime value) {
            return DateTimeFormatter.ISO_TIME.format(value);
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

    }

}
