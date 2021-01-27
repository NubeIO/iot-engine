package com.nubeiot.core.rpc.watcher;

import java.util.Locale;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for trigger option, that is based on {@code io.github.zero88.qwe.scheduler.trigger.TriggerModel}
 *
 * @see io.github.zero88.qwe.scheduler.trigger.TriggerModel
 */
@Getter
@Builder
@Jacksonized
public final class TriggerOption implements JsonData, EnumType {

    public static final String CRON_TRIGGER = "CRON";
    public static final String PERIODIC_TRIGGER = "PERIODIC";

    @NonNull
    @Accessors(fluent = true)
    private final String type;
    /**
     * Cron expression
     *
     * @see #CRON_TRIGGER
     */
    private final String expression;
    /**
     * Timezone Id
     *
     * @see #CRON_TRIGGER
     */
    private final String timezone;

    /**
     * Specify a repeat interval in seconds - which will then be multiplied by 1000 to produce milliseconds.
     *
     * @see #PERIODIC_TRIGGER
     */
    private final int intervalInSeconds;
    /**
     * Specify a the number of time the trigger will repeat - total number of firings will be this number + 1.
     *
     * @apiNote repeat forever will be set as -1
     * @see #PERIODIC_TRIGGER
     */
    private final int repeat;

    public static TriggerOptionBuilder builder() {return new TriggerOptionBuilder();}

    public static class TriggerOptionBuilder {

        public TriggerOption build() {
            final String t = type.toUpperCase(Locale.ENGLISH);
            if (t.equals(CRON_TRIGGER) || t.equals(PERIODIC_TRIGGER)) {
                return new TriggerOption(type, expression, timezone, intervalInSeconds, repeat);
            }
            throw new IllegalArgumentException(
                "Invalid trigger type. Only supports: [ " + CRON_TRIGGER + ", " + PERIODIC_TRIGGER + " ]");
        }

    }

}
