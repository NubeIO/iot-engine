package com.nubeiot.scheduler.job;

import org.quartz.JobExecutionContext;

import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;

public final class EventJob extends AbstractVertxJob<EventJobModel> {

    @Override
    public void execute(JobExecutionContext context) {
        EventJobModel jobModel = getJobModel(context.getMergedJobDataMap());
        ReplyEventHandler handler = null;
        if (jobModel.getProcess().getPattern() == EventPattern.REQUEST_RESPONSE) {
            handler = ReplyEventHandler.builder()
                                       .system(jobModel.type().name())
                                       .action(jobModel.getProcess().getAction())
                                       .success(monitor(jobModel, jobModel.getCallback()))
                                       .error(errorMonitor(jobModel))
                                       .build();
        }
        controller.request(jobModel.getProcess().getAddress(), jobModel.getProcess().getPattern(),
                           jobModel.getProcess().payload(), handler);
    }

}
