package com.nubeiot.edge.connector.bonescript.jobs;

import static com.nubeiot.edge.connector.bonescript.Historian.CONTEXT_DB;
import static com.nubeiot.edge.connector.bonescript.Historian.CONTEXT_ID;
import static com.nubeiot.edge.connector.bonescript.Historian.CONTEXT_POINT;
import static com.nubeiot.edge.connector.bonescript.Historian.CONTEXT_QUERY_EXECUTOR;
import static com.nubeiot.edge.connector.bonescript.Historian.CONTEXT_VERTX;
import static com.nubeiot.edge.connector.bonescript.constants.Constants.OUTGOING_PORT;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DATA;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.HISTORIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.HISTORY_SETTINGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.NAME;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SIZE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VAL;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.nubeiot.edge.connector.bonescript.utils.DittoDBUtils;
import com.nubeiot.edge.connector.bonescript.utils.HttpUtils;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

public class RecordPeriodicJob implements Job {

    public static int HISTORY_SIZE = 672;
    public static int MAX_BREAK_LOOP = 100000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            String id = jobDataMap.getString(CONTEXT_ID);
            JsonObject db = new JsonObject(jobDataMap.getString(CONTEXT_DB));
            JsonObject point = new JsonObject(jobDataMap.getString(CONTEXT_POINT));

            Vertx vertx = (Vertx) context.getScheduler().getContext().get(CONTEXT_VERTX);
            JDBCRXGenericQueryExecutor queryExecutor = (JDBCRXGenericQueryExecutor) context.getScheduler()
                                                                                           .getContext()
                                                                                           .get(CONTEXT_QUERY_EXECUTOR);
            long fireTime = context.getFireTime().getTime();

            JsonObject history = db.getJsonObject(THING)
                                   .getJsonObject(FEATURES)
                                   .getJsonObject(HISTORIES)
                                   .getJsonObject(PROPERTIES)
                                   .getJsonObject(id, new JsonObject());

            if (!history.containsKey(DATA)) {
                history.put(DATA, new JsonArray());
            }
            if (!history.containsKey(NAME)) {
                history.put(NAME, id);
            }

            int value = point.getInteger(VALUE);
            history.getJsonArray(DATA).add(new JsonObject().put(TS, fireTime).put(VAL, value));

            int historySize = HISTORY_SIZE;
            if (point.getJsonObject(HISTORY_SETTINGS).containsKey(SIZE)) {
                historySize = point.getJsonObject(HISTORY_SETTINGS).getInteger(SIZE);
            }

            int breakLoop = 0;
            while (history.getJsonArray(DATA).size() > historySize && breakLoop < MAX_BREAK_LOOP) {
                history.getJsonArray(DATA).remove(0);
                breakLoop++;
            }

            db.getJsonObject(THING)
              .getJsonObject(FEATURES)
              .getJsonObject(HISTORIES)
              .getJsonObject(PROPERTIES)
              .put(id, history);

            queryExecutor.executeAny(c -> DittoDBUtils.updateDittoData(c, db)).subscribe();
            logger.info("Periodic - Point '{}' value written to history", id);

            String uri = "http://localhost:" + OUTGOING_PORT + "/history";
            HttpUtils.post(vertx, uri, new JsonObject().put("id", id).put("ts", fireTime).put("val", value));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
