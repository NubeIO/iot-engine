package com.nubeiot.edge.connector.bonescript;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.nubeiot.core.TestBase;
import com.nubeiot.edge.connector.bonescript.mock.MyJob;

import io.vertx.core.json.JsonObject;

public class QuartzExampleTest extends TestBase {

    @Test
    public void testPrintEveryTwoSecond() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        JobDetail jobDetail = newJob(MyJob.class).withIdentity("job", "myJob").build();
        Trigger trigger = newTrigger().withIdentity("trigger", "inTwoSecond")
                                      .startNow()
                                      .withSchedule(cronSchedule("0/2 * * * * ?"))
                                      .build();
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.getContext().put("db", new JsonObject().put("say", "hello"));

        scheduler.start();

        // If we wanna see the output on console, do uncomment

        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        scheduler.clear();
    }

}
