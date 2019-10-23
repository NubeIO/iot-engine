package com.nubeiot.edge.connector.bacnet.dto;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

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
    private final boolean persist;
    private final boolean detail;
    private final Duration duration;

    public static DiscoverOptions from(long maxTimeoutInMS, @NonNull RequestData requestData) {
        return from(maxTimeoutInMS, requestData.getFilter());
    }

    public static DiscoverOptions from(long maxTimeoutInMS, JsonObject filter) {
        JsonObject f = Optional.ofNullable(filter).orElseGet(JsonObject::new);
        final Optional<Long> timeoutOpt = Functions.getIfThrow(
            () -> Functions.toLong().apply(Strings.toString(f.getValue(Fields.timeout))));
        final Optional<TimeUnit> timeUnitOpt = Functions.getIfThrow(
            () -> TimeUnit.valueOf(Strings.toString(f.getValue(Fields.timeUnit)).toUpperCase()));
        final Duration duration = Functions.getIfThrow(
            () -> Duration.parse(Strings.toString(f.getValue(Fields.duration)))).orElse(Duration.ofMinutes(15));
        final boolean persist = Boolean.parseBoolean(Strings.toString(f.getValue(Fields.persist)));
        final boolean detail = Boolean.parseBoolean(Strings.toString(f.getValue(Fields.detail)));
        final TimeUnit timeUnit = timeoutOpt.isPresent() ? timeUnitOpt.orElse(TimeUnit.SECONDS) : TimeUnit.MILLISECONDS;
        final long maxTimeout = timeUnit.convert(maxTimeoutInMS, TimeUnit.MILLISECONDS);
        final long defTimeout = timeUnit.convert(DEFAULT_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS);
        final long timeout = Math.min(timeoutOpt.orElse(defTimeout), maxTimeout);
        return DiscoverOptions.builder()
                              .timeout(timeout)
                              .timeUnit(timeUnit)
                              .detail(detail)
                              .persist(persist)
                              .duration(duration)
                              .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
