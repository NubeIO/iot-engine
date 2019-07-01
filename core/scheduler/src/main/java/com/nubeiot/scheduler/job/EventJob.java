package com.nubeiot.scheduler.job;

import java.util.Objects;
import java.util.function.Consumer;

import org.quartz.JobExecutionContext;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;

public final class EventJob extends AbstractVertxJob<EventJobModel> {

    @Override
    public void execute(JobExecutionContext context) {
        EventJobModel jobModel = this.getJobModel(context.getMergedJobDataMap());
        ReplyEventHandler handler = null;
        if (jobModel.getProcess().getPattern() == EventPattern.REQUEST_RESPONSE) {
            handler = ReplyEventHandler.builder()
                                       .system(jobModel.type().name())
                                       .action(jobModel.getPayload().getAction())
                                       .success(callback(jobModel.getCallback()))
                                       .build();
        }
        controller.request(jobModel.getProcess().getAddress(), jobModel.getProcess().getPattern(),
                           jobModel.getPayload(), handler);
    }

    private Consumer<EventMessage> callback(EventModel step) {
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
