package com.nubeiot.edge.connector.bonescript;

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

import com.nubeiot.edge.connector.bonescript.jobs.RecordPeriodicJob;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import lombok.NonNull;

public class Historian {

    public final static String CONTEXT_VERTX = "vertx";
    public final static String CONTEXT_DB = "db";
    public final static String CONTEXT_QUERY_EXECUTOR = "queryExecutor";
    public final static String CONTEXT_ID = "id";
    public final static String CONTEXT_POINT = "value";
    private final static Logger logger = LoggerFactory.getLogger(Historian.class);
    private static List<ScheduleJob> scheduledJobs = new ArrayList<>();

    public static void init(@NonNull Vertx vertx, @NonNull JsonObject db,
                            @NonNull JDBCRXGenericQueryExecutor queryExecutor) {
        new Historian();
        JsonObject points = db.getJsonObject(THING)
                              .getJsonObject(FEATURES)
                              .getJsonObject(POINTS)
                              .getJsonObject(PROPERTIES);

        if (points != null) {
            points.forEach(point -> {
                JsonObject value = new JsonObject(point.getValue().toString());
                if (value.containsKey(HISTORY_SETTINGS)) {
                    if (value.getJsonObject(HISTORY_SETTINGS).containsKey(TYPE) &&
                        value.getJsonObject(HISTORY_SETTINGS).getString(TYPE).equals(PERIODIC)) {

                        if (value.getJsonObject(HISTORY_SETTINGS).containsKey(SCHEDULE)) {
                            String schedule = value.getJsonObject(HISTORY_SETTINGS).getString(SCHEDULE);
                            addScheduledJob(vertx, db, queryExecutor, point.getKey(), value, schedule);
                        }
                    }
                }
            });
        }
    }

    private static void addScheduledJob(Vertx vertx, JsonObject db, JDBCRXGenericQueryExecutor queryExecutor, String id,
                                        JsonObject value, String schedule) {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler;
        try {
            scheduler = schedulerFactory.getScheduler();

            // Pass those attributes on Job contexts whose values are different from one job to another
            JobDetail jobDetail = newJob(RecordPeriodicJob.class).usingJobData(CONTEXT_ID, id)
                                                                 .usingJobData(CONTEXT_DB, db.encode())
                                                                 .usingJobData(CONTEXT_POINT, value.encode())
                                                                 .build();
            Trigger trigger = newTrigger().startNow().withSchedule(cronSchedule(schedule)).build();

            scheduler.getContext().put(CONTEXT_VERTX, vertx);
            scheduler.getContext().put(CONTEXT_QUERY_EXECUTOR, queryExecutor);
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
