package com.nubeiot.scheduler;

import java.util.List;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventContractor.Param;
import com.nubeiot.core.event.EventHandler;

import lombok.NonNull;

public class RegisterScheduleListener implements EventHandler {

    private final QuartzSchedulerContext context;

    RegisterScheduleListener(QuartzSchedulerContext context) {
        this.context = context;
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return QuartzSchedulerContext.REGISTER_ACTION;
    }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(@Param("trigger_id") String triggerId, @Param("job_id") String data) {
        return new JsonObject().put("id", triggerId).put("request", data);
    }

    @EventContractor(action = EventAction.REMOVE)
    public JsonObject remove(@Param("trigger_id") String triggerId, @Param("job_id") String jobId) {
        return new JsonObject().put("trigger_id", triggerId).put("job_id", jobId);
    }

}
