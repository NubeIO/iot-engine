package com.nubeiot.scheduler.job;

import java.util.Objects;

import org.quartz.JobKey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.scheduler.job.JobModel.AbstractJobModel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = EventJobModel.Builder.class)
public final class EventJobModel extends AbstractJobModel {

    @NonNull
    private final DeliveryEvent process;
    private final DeliveryEvent callback;

    private EventJobModel(JobKey key, DeliveryEvent process, DeliveryEvent callback, boolean forwardIfFailure) {
        super(key, JobType.EVENT_JOB, forwardIfFailure);
        this.process = process;
        this.callback = callback;
    }

    @Override
    public Class<EventJob> implementation() { return EventJob.class; }

    @Override
    public String toString() {
        return Strings.format("Type: \"{0}\" - Process Address: \"{1}\" - Callback Address: \"{2}\"", type(),
                              process.getAddress(), Objects.isNull(callback) ? "" : callback.getAddress());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractJobModelBuilder<EventJobModel, Builder> {

        public EventJobModel build() {
            if (Objects.nonNull(callback) && callback.getPattern() == EventPattern.REQUEST_RESPONSE) {
                throw new NubeException(ErrorCode.INVALID_ARGUMENT,
                                        "Callback Pattern doesn't support " + EventPattern.REQUEST_RESPONSE);
            }
            return new EventJobModel(key(), process, callback, isForwardIfFailure());
        }

    }

}
