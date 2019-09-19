package com.nubeiot.iotdata.dto;

import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Functions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Point Priority Value
 *
 * @see <a href="https://project-haystack.org/tag/writeLevel">HayStack Write Level</a>
 * @see <a href="https://store.chipkin.com/articles/bacnet-what-is-a-bacnet-priority-array/">BACNet priority array</a>
 */
@NoArgsConstructor
public final class PointPriorityValue implements JsonData {

    public static final int DEFAULT_PRIORITY = 16;
    private static final String INVALID_PRIORITY = "Priority is only in range [1, 17]";
    private static final int MIN_PRIORITY = 1;
    private static final int MAX_PRIORITY = 17;
    private static final String INVALID_VALUE = "Value must be number";
    private final SortedMap<Integer, Double> val = new TreeMap<>();

    @JsonCreator
    PointPriorityValue(Map<Object, Object> map) {
        val.putAll(map.entrySet()
                      .stream()
                      .collect(TreeMap::new, (m, entry) -> m.put(validateAndGetKey(entry.getKey()),
                                                                 validateAndGetValue(entry.getValue())), Map::putAll));
    }

    public PointPriorityValue add(int value) {
        return add((double) value);
    }

    public PointPriorityValue add(Double value) {
        return add(DEFAULT_PRIORITY, value);
    }

    public PointPriorityValue add(int priority, int value) {
        return add(priority, (double) value);
    }

    public PointPriorityValue add(int priority, Double value) {
        this.val.put(validateAndGet(priority), value);
        return this;
    }

    public Double get() {
        return get(DEFAULT_PRIORITY);
    }

    public Double get(int priority) {
        return val.get(validateAndGet(priority));
    }

    private static boolean isValid(int priority) {
        return priority <= MAX_PRIORITY && priority >= MIN_PRIORITY;
    }

    private int validateAndGetKey(@NonNull Object priority) {
        return validateAndGet(Functions.getOrThrow(() -> Functions.toInt().apply(priority.toString()),
                                                   () -> new IllegalArgumentException(INVALID_PRIORITY)));
    }

    private Double validateAndGetValue(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Functions.getOrThrow(() -> Double.valueOf(value.toString()),
                                    () -> new IllegalArgumentException(INVALID_VALUE));
    }

    private static int validateAndGet(int priority) {
        if (isValid(priority)) {
            return priority;
        }
        throw new IllegalArgumentException(INVALID_PRIORITY);
    }

    public PointValue findHighestValue() {
        return val.entrySet()
                  .stream()
                  .filter(entry -> Objects.nonNull(entry.getValue()))
                  .findFirst()
                  .map(entry -> new PointValue(entry.getKey(), entry.getValue()))
                  .orElse(new PointValue(DEFAULT_PRIORITY, null));
    }

    @Override
    public JsonObject toJson() {
        return JsonData.MAPPER.convertValue(val, JsonObject.class);
    }

    @Override
    public String toString() {
        return toJson().encode();
    }

    public int hashCode() {
        final int PRIME_KEY = 31;
        final int PRIME_VALUE = 43;
        final int PRIME = 59;
        int result = 1;
        return val.entrySet()
                  .stream()
                  .map(entry -> entry.getKey() * PRIME_KEY + entry.getValue().hashCode() * PRIME_VALUE)
                  .reduce(result, (r, i) -> r + PRIME * i);
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PointPriorityValue)) {
            return false;
        }
        final PointPriorityValue other = (PointPriorityValue) o;
        return val.entrySet().stream().allMatch(entry -> {
            final Double v1 = val.get(entry.getKey());
            final Double v2 = other.val.get(entry.getKey());
            return v1 == null && v2 == null || (v1 != null && v1.equals(v2));
        });
    }

    @Getter
    public static class PointValue implements VertxPojo, JsonData {

        private final int priority;
        private final Double value;

        public PointValue(int priority, Double value) {
            this.priority = validateAndGet(priority);
            this.value = value;
        }

        @Override
        public PointValue fromJson(JsonObject json) {
            return new PointValue(json.getInteger("priority", DEFAULT_PRIORITY), json.getDouble("value"));
        }

        @Override
        public JsonObject toJson() {
            return JsonData.super.toJson();
        }

    }

}
