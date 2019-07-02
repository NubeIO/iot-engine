package com.nubeiot.scheduler.job;

import java.util.Objects;

import org.quartz.JobKey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = EventJobModel.Builder.class)
public final class EventJobModel implements JobModel<EventJob> {

    private final JobType type;
    private final JobKey key;
    private final EventMessage payload;
    @NonNull
    private final EventModel process;
    private final EventModel callback;

    @Override
    public JobType type() { return type; }

    @Override
    public Class<EventJob> implementation() { return EventJob.class; }

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

        public EventJobModel build() {
            key = Objects.isNull(key) ? JobModel.createKey(group, name) : key;
            payload = Objects.isNull(payload) ? EventMessage.initial(EventAction.UNKNOWN) : payload;
            return new EventJobModel(type, key, payload, process, callback);
        }

    }

}
