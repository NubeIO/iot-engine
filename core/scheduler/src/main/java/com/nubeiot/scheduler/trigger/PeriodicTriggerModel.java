package com.nubeiot.scheduler.trigger;

import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.scheduler.trigger.TriggerModel.AbstractTriggerModel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = PeriodicTriggerModel.Builder.class)
public final class PeriodicTriggerModel extends AbstractTriggerModel {

    /**
     * Specify a repeat interval in seconds - which will then be multiplied by 1000 to produce milliseconds.
     */
    private final int intervalInSeconds;
    /**
     * Specify a the number of time the trigger will repeat - total number of firings will be this number + 1.
     */
    private final int repeat;

    private PeriodicTriggerModel(TriggerType type, TriggerKey key, int intervalInSeconds, int repeat) {
        super(key, type);
        this.intervalInSeconds = intervalInSeconds;
        this.repeat = repeat;
    }

    @Override
    protected @NonNull ScheduleBuilder<SimpleTrigger> scheduleBuilder() {
        return SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalInSeconds).withRepeatCount(repeat);
    }

    @Override
    public String toString() {
        return Strings.format("Interval: {0}s | Repeat: {1}", intervalInSeconds, repeat);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractTriggerModelBuilder<PeriodicTriggerModel, Builder> {

        public PeriodicTriggerModel build() {
            return new PeriodicTriggerModel(TriggerType.PERIODIC, key(), intervalInSeconds,
                                            repeat < 0 || repeat >= Integer.MAX_VALUE
                                            ? SimpleTrigger.REPEAT_INDEFINITELY
                                            : repeat);
        }

    }

}
