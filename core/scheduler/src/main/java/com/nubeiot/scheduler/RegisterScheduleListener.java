package com.nubeiot.scheduler;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RegisterScheduleListener implements EventListener {

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
            scheduler.scheduleJob(jobDetail, trigger);
            return new JsonObject().put("trigger_key", trigger.getKey()).put("job_key", jobDetail.getKey());
        } catch (SchedulerException e) {
            return EventMessage.error(EventAction.CREATE, e).toJson();
        }
    }

    @EventContractor(action = EventAction.REMOVE)
    public JsonObject remove(@Param("trigger_group") String triggerGroup, @Param("trigger_name") String triggerName) {
        if (Strings.isBlank(triggerName)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Missing trigger name");
        }
        try {
            return new JsonObject().put("success",
                                        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName, triggerGroup)));
        } catch (SchedulerException e) {
            return EventMessage.error(EventAction.REMOVE, e).toJson();
        }
    }

}
