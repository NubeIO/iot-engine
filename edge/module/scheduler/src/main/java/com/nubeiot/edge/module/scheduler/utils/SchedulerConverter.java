package com.nubeiot.edge.module.scheduler.utils;

import java.util.Objects;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.job.EventJobModel;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.job.JobType;
import com.nubeiot.scheduler.trigger.CronTriggerModel;
import com.nubeiot.scheduler.trigger.PeriodicTriggerModel;
import com.nubeiot.scheduler.trigger.TriggerModel;
import com.nubeiot.scheduler.trigger.TriggerType;

import lombok.NonNull;

public interface SchedulerConverter {

    static <T> T convert(String objType, JsonObject request, Function<JsonObject, T> converter) {
        try {
            return converter.apply(request);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                if (e.getCause() instanceof InvalidDefinitionException) {
                    throw NubeExceptionConverter.friendly(e.getCause().getCause());
                }
                if (e.getCause() instanceof InvalidTypeIdException) {
                    throw new IllegalArgumentException(
                        "Not yet supported " + objType + " type: " + request.getString("type"));
                }
            }
            throw NubeExceptionConverter.friendly(e);
        }
    }

    interface JobConverter {

        static JobEntity convert(@NonNull JsonObject object) {
            return SchedulerConverter.convert("job", object,
                                              (json) -> convert(JsonData.convertLenient(json, JobModel.class)));
        }

        static JobEntity convert(@NonNull JobModel job) {
            return new JobEntity().setName(job.getKey().getName())
                                  .setGroup(job.getKey().getGroup())
                                  .setType(job.type())
                                  .setForwardIfFailure(job.forwardIfFailure())
                                  .setDetail(job.toDetail());
        }

        static JobModel convert(@NonNull JobEntity entity) {
            final JobType type = Objects.requireNonNull(entity.getType(), "Job Type cannot be null");
            if (JobType.EVENT_JOB.equals(type)) {
                JsonObject detail = Objects.requireNonNull(entity.getDetail(), "Job detail cannot be null");
                return EventJobModel.builder()
                                    .group(entity.getGroup())
                                    .name(entity.getName())
                                    .forwardIfFailure(entity.getForwardIfFailure())
                                    .process(DeliveryEvent.from(detail.getJsonObject("process")))
                                    .callback(DeliveryEvent.from(detail.getJsonObject("callback")))
                                    .build();
            }
            throw new IllegalArgumentException("Not yet supported job type: " + type.type());
        }

        static JobEntity validate(@NonNull JobEntity entity) {
            convert(entity);
            return entity;
        }

    }


    interface TriggerConverter {

        static TriggerEntity convert(@NonNull JsonObject object) {
            return SchedulerConverter.convert("trigger", object,
                                              json -> convert(JsonData.convertLenient(json, TriggerModel.class)));
        }

        static TriggerEntity convert(@NonNull TriggerModel trigger) {
            return new TriggerEntity().setName(trigger.getKey().getName())
                                      .setGroup(trigger.getKey().getGroup())
                                      .setType(trigger.type())
                                      .setDetail(trigger.toDetail())
                                      .setThread(trigger.logicalThread());
        }

        static TriggerModel convert(@NonNull TriggerEntity entity) {
            final JsonObject detail = Objects.requireNonNull(entity.getDetail(), "Trigger detail cannot be null");
            if (entity.getType().equals(TriggerType.CRON)) {
                return CronTriggerModel.builder()
                                       .group(entity.getGroup())
                                       .name(entity.getName())
                                       .expr(detail.getString("expression"))
                                       .tz(detail.getString("timezone"))
                                       .build();
            }
            if (entity.getType().equals(TriggerType.PERIODIC)) {
                return PeriodicTriggerModel.builder()
                                           .group(entity.getGroup())
                                           .name(entity.getName())
                                           .intervalInSeconds(detail.getInteger("intervalInSeconds"))
                                           .repeat(detail.getInteger("repeat"))
                                           .build();
            }
            throw new IllegalArgumentException("Not yet supported trigger type: " + entity.getType().type());
        }

        static TriggerEntity validate(TriggerEntity pojo) {
            convert(pojo);
            return pojo;
        }

    }

}
