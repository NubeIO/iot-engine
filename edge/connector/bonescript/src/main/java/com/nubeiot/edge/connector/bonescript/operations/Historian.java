package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.HISTORY_SETTINGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PERIODIC;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.POINTS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SCHEDULE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TYPE;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.nubeiot.edge.connector.bonescript.BoneScriptEntityHandler;
import com.nubeiot.edge.connector.bonescript.MultiThreadDittoDB;
import com.nubeiot.edge.connector.bonescript.ScheduleJob;
import com.nubeiot.edge.connector.bonescript.jobs.RecordPeriodicJob;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import lombok.NonNull;

public class Historian {

    public static final String CONTEXT_VERTX = "vertx";
    public static final String CONTEXT_ENTITY_HANDLER = "entityHandler";
    public static final String CONTEXT_DITTO_DB = "dittoDB";
    public static final String CONTEXT_ID = "id";
    private static final Logger logger = LoggerFactory.getLogger(Historian.class);
    private static List<ScheduleJob> scheduledJobs = new ArrayList<>();

    public static void init(@NonNull Vertx vertx, @NonNull BoneScriptEntityHandler entityHandler,
                            @NonNull MultiThreadDittoDB multiThreadDittoDB, @NonNull JsonObject db) {
        JsonObject points = db.getJsonObject(THING)
                              .getJsonObject(FEATURES)
                              .getJsonObject(POINTS)
                              .getJsonObject(PROPERTIES);

        if (points != null) {
            points.forEach(point -> {
                JsonObject pointValue = new JsonObject(point.getValue().toString());
                if (pointValue.containsKey(HISTORY_SETTINGS)) {
                    if (pointValue.getJsonObject(HISTORY_SETTINGS).containsKey(TYPE) &&
                        pointValue.getJsonObject(HISTORY_SETTINGS).getString(TYPE).equals(PERIODIC)) {

                        if (pointValue.getJsonObject(HISTORY_SETTINGS).containsKey(SCHEDULE)) {
                            String schedule = pointValue.getJsonObject(HISTORY_SETTINGS).getString(SCHEDULE);
                            addScheduledJob(vertx, entityHandler, multiThreadDittoDB, point.getKey(), schedule);
                        }
                    }
                }
            });
        }
    }

    private static void addScheduledJob(Vertx vertx, BoneScriptEntityHandler entityHandler,
                                        MultiThreadDittoDB multiThreadDittoDB, String id, String schedule) {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler;
        try {
            scheduler = schedulerFactory.getScheduler();

            // Pass those attributes on Job contexts whose values are different from one job to another
            JobDetail jobDetail = newJob(RecordPeriodicJob.class).usingJobData(CONTEXT_ID, id).build();
            Trigger trigger = newTrigger().startNow().withSchedule(cronSchedule(schedule)).build();

            scheduler.getContext().put(CONTEXT_VERTX, vertx);
            scheduler.getContext().put(CONTEXT_ENTITY_HANDLER, entityHandler);
            scheduler.getContext().put(CONTEXT_DITTO_DB, multiThreadDittoDB);
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();

            ScheduleJob scheduleJob = new ScheduleJob(schedule);
            scheduleJob.setScheduler(scheduler);
            scheduledJobs.add(scheduleJob);
            logger.info("Point {} history schedule created and started", id);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
