package com.nubeiot.edge.connector.bonescript.mock;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import io.vertx.core.json.JsonObject;

public class MyJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JsonObject jsonObject = new JsonObject(context.getScheduler().getContext().get("db").toString());
            System.out.println("DB value is: " + jsonObject.encode());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
