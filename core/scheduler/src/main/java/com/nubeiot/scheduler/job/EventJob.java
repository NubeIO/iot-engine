package com.nubeiot.scheduler.job;

import java.util.Objects;
import java.util.function.Consumer;

import org.quartz.JobExecutionContext;

import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventMessage;
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
                                       .action(jobModel.getProcess().getAction())
                                       .success(callback(jobModel.getProcess(), jobModel.getCallback()))
                                       .build();
        }
        controller.request(jobModel.getProcess().getAddress(), jobModel.getProcess().getPattern(),
                           jobModel.getProcess().payload(), handler);
    }

    private Consumer<EventMessage> callback(DeliveryEvent process, DeliveryEvent step) {
        if (Objects.isNull(step)) {
            return msg -> {
                if (msg.isError()) {
                    logger.error("Error in job: " + msg.getError().toJson());
                } else {
                    logger.info("Receive message {}", msg.toJson());
                }
            };
        }

        return msg -> {
            logger.info("Forward JOB result from address: '{}' -> '{}'", process.getAddress(), step.getAddress());
            if (logger.isTraceEnabled()) {
                logger.trace("JOB Result: {}", msg.toJson());
            }
            controller.response(step.getAddress(), step.getPattern(), EventMessage.override(msg, step.getAction()));
        };
    }

}
