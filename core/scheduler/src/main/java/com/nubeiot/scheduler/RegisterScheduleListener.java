package com.nubeiot.scheduler;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.utils.Key;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RegisterScheduleListener implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterScheduleListener.class);
    private final Scheduler scheduler;
    private final Set<EventAction> events;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.unmodifiableSet(events);
    }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(@Param(SchedulerRequestData.JOB_KEY) JobModel jobModel,
                             @Param(SchedulerRequestData.TRIGGER_KEY) TriggerModel triggerModel) {
        try {
            final JobDetail jobDetail = jobModel.toJobDetail();
            final Trigger trigger = triggerModel.toTrigger();
            LOGGER.info("Scheduler register | Job: {} | Trigger: {}", jobModel.toJson(), triggerModel.toJson());
            Date firstFire = scheduler.scheduleJob(jobDetail, trigger);
            return new JsonObject().put("trigger", keyToJson(trigger.getKey()))
                                   .put("job", keyToJson(jobDetail.getKey()))
                                   .put("first_fire_time", DateTimes.format(firstFire));
        } catch (SchedulerException e) {
            throw new NubeException(
                e instanceof ObjectAlreadyExistsException ? ErrorCode.SERVICE_ERROR : ErrorCode.SERVICE_ERROR,
                "Cannot add trigger and job in scheduler", e);
        }
    }

    @EventContractor(action = EventAction.REMOVE)
    public JsonObject remove(@Param("job_group") String jobGroup, @Param("job_name") String jobName) {
        if (Strings.isBlank(jobName)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Missing job name");
        }
        final JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            return new JsonObject().put("unschedule", scheduler.deleteJob(jobKey));
        } catch (SchedulerException e) {
            throw new NubeException(ErrorCode.SERVICE_ERROR, "Cannot remove job id " + jobKey.toString(), e);
        }
    }

    private JsonObject keyToJson(@NonNull Key key) {
        return new JsonObject().put("group", key.getGroup()).put("name", key.getName());
    }

}
