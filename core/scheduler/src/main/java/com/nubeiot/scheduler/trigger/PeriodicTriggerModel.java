package com.nubeiot.scheduler.trigger;

import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.scheduler.trigger.TriggerModel.AbstractTriggerModel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = PeriodicTriggerModel.Builder.class)
public final class PeriodicTriggerModel extends AbstractTriggerModel {

    private final int intervalInSeconds;
    private final int repeat;

    PeriodicTriggerModel(TriggerType type, TriggerKey key, int intervalInSeconds, int repeat) {
        super(key, type);
        this.intervalInSeconds = intervalInSeconds;
        this.repeat = repeat;
    }

    @Override
    protected @NonNull ScheduleBuilder<SimpleTrigger> scheduleBuilder() {
        return SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(intervalInSeconds).withRepeatCount(repeat);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractTriggerBuilder {

        public PeriodicTriggerModel build() {
            return new PeriodicTriggerModel(TriggerType.PERIODIC, TriggerModel.createKey(group, name),
                                            intervalInSeconds, repeat);
        }

    }

}
