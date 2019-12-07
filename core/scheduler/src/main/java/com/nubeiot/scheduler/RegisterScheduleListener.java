package com.nubeiot.scheduler;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.utils.Key;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.DateTimes.Iso8601Formatter;
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

    @EventContractor(action = EventAction.GET_ONE)
    public JsonObject get(@Param(SchedulerRequestData.JOB_KEY) JobModel jobModel,
                          @Param(SchedulerRequestData.TRIGGER_KEY) TriggerModel triggerModel) {
        try {
            final JobDetail jobDetail = jobModel.toJobDetail();
            final Trigger trigger = triggerModel.toTrigger();
            return scheduler.getTriggersOfJob(jobDetail.getKey())
                            .stream()
                            .filter(tr -> tr.getKey().equals(trigger.getKey()))
                            .map(tr -> new JsonObject().put("trigger", keyToJson(tr.getKey()))
                                                       .put("job", keyToJson(jobDetail.getKey()))
                                                       .put("next_fire_time", computeTime(tr.getNextFireTime(), tr))
                                                       .put("prev_fire_time",
                                                            computeTime(tr.getPreviousFireTime(), tr)))
                            .findFirst()
                            .orElse(new JsonObject());
        } catch (SchedulerException e) {
            throw new NubeException(ErrorCode.SERVICE_ERROR, "Cannot get trigger and job in scheduler", e);
        }
    }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(@Param(SchedulerRequestData.JOB_KEY) JobModel jobModel,
                             @Param(SchedulerRequestData.TRIGGER_KEY) TriggerModel triggerModel) {
        try {
            final JobDetail jobDetail = jobModel.toJobDetail();
            final Trigger trigger = triggerModel.toTrigger();
            LOGGER.info("Scheduler register | Job: {} | Trigger: {}", jobModel.toJson(), triggerModel.toJson());
            final TriggerKey key = trigger.getKey();
            if (scheduler.checkExists(key)) {
                final JobKey jobKey = scheduler.getTrigger(key).getJobKey();
                throw new AlreadyExistException("Trigger " + key + " is already assigned to another job " + jobKey);
            }
            JsonObject firstFire;
            if (scheduler.checkExists(jobDetail.getKey())) {
                final Trigger refTrigger = trigger.getTriggerBuilder()
                                                  .forJob(jobDetail)
                                                  .usingJobData(jobDetail.getJobDataMap())
                                                  .build();
                firstFire = computeTime(scheduler.scheduleJob(refTrigger), trigger);
            } else {
                firstFire = computeTime(scheduler.scheduleJob(jobDetail, trigger), trigger);
            }
            return new JsonObject().put("trigger", keyToJson(key))
                                   .put("job", keyToJson(jobDetail.getKey())).put("first_fire_time", firstFire);
        } catch (SchedulerException e) {
            throw new NubeException(
                e instanceof ObjectAlreadyExistsException ? ErrorCode.ALREADY_EXIST : ErrorCode.SERVICE_ERROR,
                "Cannot add trigger and job in scheduler", e);
        }
    }

    @EventContractor(action = EventAction.REMOVE)
    public JsonObject remove(@Param(SchedulerRequestData.JOB_KEY) JsonObject jobKey) {
        final JobKey key = JobKey.jobKey(jobKey.getString("name"), jobKey.getString("group", null));
        try {
            return new JsonObject().put("unschedule", scheduler.deleteJob(key));
        } catch (SchedulerException e) {
            throw new NubeException(ErrorCode.SERVICE_ERROR, "Cannot remove job id " + key.toString(), e);
        }
    }

    private JsonObject keyToJson(@NonNull Key key) {
        return new JsonObject().put("group", key.getGroup()).put("name", key.getName());
    }

    private JsonObject computeTime(Date fireTime, Trigger trigger) {
        if (Objects.isNull(fireTime)) {
            return null;
        }
        if (trigger instanceof CronTrigger) {
            final TimeZone timeZone = ((CronTrigger) trigger).getTimeZone();
            return Iso8601Formatter.format(fireTime, timeZone);
        }
        return Iso8601Formatter.format(fireTime, null);
    }

}
