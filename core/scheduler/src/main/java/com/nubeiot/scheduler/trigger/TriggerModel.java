package com.nubeiot.scheduler.trigger;

import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.utils.Key;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes( {
    @JsonSubTypes.Type(value = CronTriggerModel.class, name = "CRON"),
    @JsonSubTypes.Type(value = PeriodicTriggerModel.class, name = "PERIODIC")
})
public interface TriggerModel extends JsonData {

    static TriggerKey createKey(String group, String name) {
        return new TriggerKey(Strings.isBlank(name) ? Key.createUniqueName(group) : name, group);
    }

    @JsonUnwrapped
    TriggerKey getKey();

    @JsonProperty(value = "type", required = true)
    TriggerType type();

    Trigger toTrigger();

    enum TriggerType {
        CRON, PERIODIC
    }


    @RequiredArgsConstructor
    abstract class AbstractTriggerModel implements TriggerModel {

        @Getter
        private final TriggerKey key;
        private final TriggerType type;

        @NonNull
        protected abstract ScheduleBuilder<? extends Trigger> scheduleBuilder();

        @Override
        public final TriggerType type() { return type; }

        @Override
        public final Trigger toTrigger() {
            return TriggerBuilder.newTrigger().withIdentity(getKey()).withSchedule(scheduleBuilder()).build();
        }

        public static abstract class AbstractTriggerBuilder {

            protected String name;
            protected String group;

            public <B extends AbstractTriggerBuilder> B group(String group) {
                this.group = group;
                return (B) this;
            }

            public <B extends AbstractTriggerBuilder> B name(String name) {
                this.name = name;
                return (B) this;
            }

            public abstract <T extends TriggerModel> T build();

        }

    }

}
