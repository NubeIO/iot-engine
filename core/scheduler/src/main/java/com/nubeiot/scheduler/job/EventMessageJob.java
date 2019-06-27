package com.nubeiot.scheduler.job;

import java.util.Objects;
import java.util.function.Consumer;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;

public class EventMessageJob implements Job {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getMergedJobDataMap();
        EventController controller = (EventController) jobDataMap.get("eventController");
        EventMessage payload = (EventMessage) jobDataMap.get("payload");
        EventModel jobStep = (EventModel) jobDataMap.get("jobStep");
        EventModel postJobStep = (EventModel) jobDataMap.get("postJobStep");
        if (jobStep.getPattern() == EventPattern.REQUEST_RESPONSE) {
            ReplyEventHandler handler = ReplyEventHandler.builder()
                                                         .system("EVENT-MESSAGE-JOB")
                                                         .action(payload.getAction())
                                                         .success(callback(controller, postJobStep))
                                                         .build();
            controller.request(jobStep.getAddress(), jobStep.getPattern(), payload, handler);
        } else {
            controller.request(jobStep.getAddress(), jobStep.getPattern(), payload);
        }
    }

    private Consumer<EventMessage> callback(EventController controller, EventModel step) {
        if (Objects.isNull(step)) {
            return msg -> {
                if (msg.isError()) {
                    logger.error("Error in job: " + msg.getError().toJson());
                } else {
                    logger.info("Receive message {}", msg.toJson());
                }
            };
        }
        return msg -> controller.response(step.getAddress(), step.getPattern(), msg);
    }

}
