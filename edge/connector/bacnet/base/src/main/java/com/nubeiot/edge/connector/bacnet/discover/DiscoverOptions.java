package com.nubeiot.edge.connector.bacnet.discover;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.github.zero88.utils.Functions;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = DiscoverOptions.Builder.class)
@FieldNameConstants(level = AccessLevel.PACKAGE)
public final class DiscoverOptions implements JsonData {

    public static final long DEFAULT_TIMEOUT_IN_MS = 3000;

    private final long timeout;
    private final TimeUnit timeUnit;
    private final boolean detail;
    private final Duration duration;
    private final int minItem;
    private final int maxItem;
    private final boolean force;

    public static DiscoverOptions from(long maxTimeoutInMS, @NonNull RequestData requestData) {
        return from(maxTimeoutInMS, requestData.filter());
    }

    public static DiscoverOptions from(long maxTimeoutInMS, JsonObject filter) {
        final JsonObject f = Optional.ofNullable(filter).orElseGet(JsonObject::new);
        final Optional<Long> timeoutOpt = Functions.getIfThrow(
            () -> Functions.toLong().apply(Strings.toString(f.getValue(Fields.timeout))));
        final Optional<TimeUnit> timeUnitOpt = Functions.getIfThrow(
            () -> TimeUnit.valueOf(Strings.toString(f.getValue(Fields.timeUnit)).toUpperCase()));
        final Duration duration = Functions.getIfThrow(
            () -> Duration.parse(Strings.toString(f.getValue(Fields.duration)))).orElse(Duration.ofMinutes(15));
        final int minItem = Functions.getIfThrow(
            () -> Functions.toInt().apply(Strings.toString(f.getValue(Fields.minItem)))).orElse(0);
        final int maxItem = Functions.getIfThrow(
            () -> Functions.toInt().apply(Strings.toString(f.getValue(Fields.maxItem)))).orElse(-1);
        final boolean detail = Boolean.parseBoolean(Strings.toString(f.getValue(Fields.detail)));
        final boolean force = Boolean.parseBoolean(Strings.toString(f.getValue(Fields.force)));
        final TimeUnit timeUnit = timeoutOpt.isPresent() ? timeUnitOpt.orElse(TimeUnit.SECONDS) : TimeUnit.MILLISECONDS;
        final long maxTimeout = timeUnit.convert(maxTimeoutInMS, TimeUnit.MILLISECONDS);
        final long defTimeout = timeUnit.convert(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        final long timeout = Math.min(timeoutOpt.orElse(defTimeout), maxTimeout);
        return DiscoverOptions.builder()
                              .timeout(timeout)
                              .timeUnit(timeUnit)
                              .detail(detail)
                              .duration(duration)
                              .minItem(Math.max(minItem, 0))
                              .maxItem(Math.max(maxItem, -1))
                              .force(force)
                              .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
