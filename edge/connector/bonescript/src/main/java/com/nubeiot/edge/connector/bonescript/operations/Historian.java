package com.nubeiot.edge.connector.bonescript.operations;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DATA;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.HISTORY_SETTINGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.NAME;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PERIODIC;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SCHEDULE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TOLERANCE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VAL;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.edge.connector.bonescript.BoneScriptEntityHandler;
import com.nubeiot.edge.connector.bonescript.DittoDBOperation;
import com.nubeiot.edge.connector.bonescript.ScheduleJob;
import com.nubeiot.edge.connector.bonescript.SingletonBBPinMapping;
import com.nubeiot.edge.connector.bonescript.jobs.RecordPeriodicJob;
import com.nubeiot.edge.connector.bonescript.utils.HttpUtils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import lombok.NonNull;

public class Historian {

    public static int HISTORY_SIZE = 672;
    public static int MAX_BREAK_LOOP = 100000;
    public static final String CONTEXT_VERTX = "vertx";
    public static final String CONTEXT_ID = "id";
    private static final Logger logger = LoggerFactory.getLogger(Historian.class);
    private static List<ScheduleJob> scheduledJobs = new ArrayList<>();

    public static void init(@NonNull Vertx vertx, @NonNull JsonObject db) {
        JsonObject points = (JsonObject) JsonUtils.getObject(db, "thing.features.points.properties");

        if (points != null) {
            points.forEach(point$ -> {
                JsonObject point = (JsonObject) point$.getValue();
                if (JsonUtils.getObject(point, "historySettings.type", "").equals(PERIODIC)) {
                    if (point.getJsonObject(HISTORY_SETTINGS).containsKey(SCHEDULE)) {
                        String schedule = point.getJsonObject(HISTORY_SETTINGS).getString(SCHEDULE);
                        addScheduledJob(vertx, point$.getKey(), schedule);
                    }
                }
            });
        }
    }

    private static void addScheduledJob(Vertx vertx, String id, String schedule) {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler;
        try {
            scheduler = schedulerFactory.getScheduler();

            // Pass those attributes on Job contexts whose values are different from one job to another
            JobDetail jobDetail = newJob(RecordPeriodicJob.class).usingJobData(CONTEXT_ID, id).build();
            Trigger trigger = newTrigger().startNow().withSchedule(cronSchedule(schedule)).build();

            scheduler.getContext().put(CONTEXT_VERTX, vertx);
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

    public static void recordCov(@NonNull BoneScriptEntityHandler entityHandler, Vertx vertx, String id,
                                 JsonObject value) {
        long timestamp = new Date().getTime();
        DittoDBOperation.syncHistory(id, Historian.createHistoryData(timestamp, value), true);
        logger.info("Periodic - Point '{}' value written to history", id);
        Historian.postHistory(vertx, id, timestamp, value);
    }

    public static boolean isHistoryWritable(String id, JsonObject point, Object value, JsonObject history) {
        int historyLength = history.getJsonArray("data").size();
        if (historyLength == 0) {
            logger.info("COV _ Point '{}' has no histories - writing value to histories");
        } else if (history.getJsonArray(DATA).getJsonObject(historyLength - 1).getValue(VAL) != value) {
            if (point.getJsonObject(HISTORY_SETTINGS).containsKey(TOLERANCE) &&
                point.getJsonObject(HISTORY_SETTINGS).getInteger(TOLERANCE) >= 0) {
                logger.info("COV - Point '{}' value inside tolerance - writing value to histories", id);
            } else {
                logger.info("COV - Point '{}' value outside tolerance - not writing value to histories", id);
                return true;
            }
        } else {
            logger.debug("COV - Point '{}' value unchanged - not writing value to histories", id);
            return true;
        }
        return false;
    }

    // If the name and data variables don't exists for the point, create them
    public static void initializeHistory(JsonObject history, String id) {
        if (!history.containsKey(DATA)) {
            history.put(DATA, new JsonArray());
        }
        if (!history.containsKey(NAME)) {
            history.put(NAME, id);
        }
    }

    public static JsonObject createHistoryData(long timestamp, Object value) {
        return new JsonObject().put(TS, timestamp).put(VAL, value);
    }

    public static void postHistory(Vertx vertx, String id, long timestamp, Object value) {
        String uri = "http://localhost:" + SingletonBBPinMapping.getInstance().getOutgoingPort() + "/history";
        HttpUtils.post(vertx, uri, new JsonObject().put("id", id).put("ts", timestamp).put("val", value));
    }

}
