package com.nubeiot.edge.connector.bonescript.jobs;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;
import static com.nubeiot.edge.connector.bonescript.operations.Historian.CONTEXT_ID;
import static com.nubeiot.edge.connector.bonescript.operations.Historian.CONTEXT_VERTX;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.edge.connector.bonescript.DittoDBOperation;
import com.nubeiot.edge.connector.bonescript.operations.Historian;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

public class RecordPeriodicJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String id = context.getJobDetail().getJobDataMap().getString(CONTEXT_ID);
            long fireTime = context.getFireTime().getTime();
            JsonObject db = DittoDBOperation.getDittoData().blockingGet();
            JsonObject point = (JsonObject) JsonUtils.getObject(db, "thing.features.points.properties." + id);
            Vertx vertx = (Vertx) context.getScheduler().getContext().get(CONTEXT_VERTX);
            int value = point.getInteger(VALUE);
            DittoDBOperation.syncHistory(id, Historian.createHistoryData(fireTime, value), false);
            logger.info("Periodic - Point '{}' value written to history", id);
            Historian.postHistory(vertx, id, fireTime, value);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
