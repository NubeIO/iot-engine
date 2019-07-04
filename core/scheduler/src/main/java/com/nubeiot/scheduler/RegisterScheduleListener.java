package com.nubeiot.scheduler;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.quartz.JobDetail;
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
    public JsonObject create(@Param("job") JobModel jobModel, @Param("trigger") TriggerModel triggerModel) {
        try {
            final JobDetail jobDetail = jobModel.toJobDetail();
            final Trigger trigger = triggerModel.toTrigger();
            LOGGER.info("Scheduler register | Job: {} | Trigger: {}", jobModel.toString(), triggerModel.toString());
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
    public JsonObject remove(@Param("trigger_group") String triggerGroup, @Param("trigger_name") String triggerName) {
        if (Strings.isBlank(triggerName)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Missing trigger name");
        }
        try {
            return new JsonObject().put("unschedule",
                                        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName, triggerGroup)));
        } catch (SchedulerException e) {
            throw new NubeException(ErrorCode.SERVICE_ERROR, "Cannot remove trigger", e);
        }
    }

    private JsonObject keyToJson(@NonNull Key key) {
        return new JsonObject().put("group", key.getGroup()).put("name", key.getName());
    }

}
