package com.nubeiot.scheduler.trigger;

import java.util.Objects;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.TriggerKey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.scheduler.TriggerModel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = CronTriggerModel.Builder.class)
public class CronTriggerModel implements TriggerModel {

    private final TriggerType type;
    private final TriggerKey key;
    private final CronExpression cronExpression;

    @Override
    public TriggerKey getKey() { return key; }

    @Override
    public TriggerType type() { return type; }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private String name;
        private String group;

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public CronTriggerModel build() {
            key = Objects.isNull(key) ? TriggerModel.createKey(group, name) : key;
            return new CronTriggerModel(type, key, cronExpression);
        }

    }

}
