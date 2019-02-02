package com.nubeiot.edge.connector.bonescript;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.DATA;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.FEATURES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.HISTORIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.HISTORY_SETTINGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PROPERTIES;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SIZE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.THING;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VAL;

import java.util.concurrent.locks.ReentrantLock;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.edge.connector.bonescript.model.Tables;
import com.nubeiot.edge.connector.bonescript.model.tables.pojos.TblDitto;
import com.nubeiot.edge.connector.bonescript.operations.Historian;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DittoDBOperation {

    private static final Logger logger = LoggerFactory.getLogger(DittoDBOperation.class);
    private static DittoDBOperation instance;
    private BoneScriptEntityHandler entityHandler;

    private DittoDBOperation(BoneScriptEntityHandler entityHandler) {
        this.entityHandler = entityHandler;
        instance = this;
    }

    public static DittoDBOperation getInstance(BoneScriptEntityHandler entityHandler) {
        if (instance == null) {
            new DittoDBOperation(entityHandler);
        }
        return instance;
    }

    public static DittoDBOperation getInstance() {
        if (instance != null) {
            return instance;
        }
        logger.error("SynchronizedDB initialization failed...");
        throw new InitializerError("SynchronizedDB initialization failed...");
    }

    public static Single<JsonObject> getDittoData() {
        return instance.entityHandler.getTblDittoDao()
                                     .findOneById(1)
                                     .map(optional -> optional.map(TblDitto::getValue))
                                     .map(o -> o.orElseThrow(() -> new NotFoundException("Value doesn't exist on DB!")))
                                     .map(JsonObject::new);
    }

    public static Single<Integer> updateDittoData(JsonObject value) {
        return instance.entityHandler.getQueryExecutor()
                                     .executeAny(c -> c.update(Tables.TBL_DITTO)
                                                       .set(Tables.TBL_DITTO.VALUE, value.encode())
                                                       .where(Tables.TBL_DITTO.ID.eq(1))
                                                       .execute());
    }

    private static ReentrantLock syncHistoryLock = new ReentrantLock();

    public static Single<Integer> syncHistory(String id, JsonObject historyLine, boolean checkWritable) {
        syncHistoryLock.lock();
        return getDittoData().flatMap(db -> {
            JsonObject point = (JsonObject) JsonUtils.getObject(db, "thing.features.points" + ".properties." + id);
            JsonObject history = (JsonObject) JsonUtils.getObject(db,
                                                                  "thing.features" + ".histories" + ".properties." + id,
                                                                  new JsonObject());
            if (checkWritable) {
                Object type = JsonUtils.getObject(point, "historySettings.type");
                if (type != null && type.toString().equalsIgnoreCase("cov")) {
                    Object value = historyLine.getValue(VAL);
                    if (!Historian.isHistoryWritable(id, point, value, history)) {
                        return Single.just(0); // just ignore history writing operation
                    }
                }
            }

            Historian.initializeHistory(history, id);
            history.getJsonArray(DATA).add(historyLine);
            int historySize = Historian.HISTORY_SIZE;
            if (point.getJsonObject(HISTORY_SETTINGS).containsKey(SIZE)) {
                historySize = point.getJsonObject(HISTORY_SETTINGS).getInteger(SIZE);
            }
            if (point.getJsonObject(HISTORY_SETTINGS).containsKey(SIZE)) {
                historySize = point.getJsonObject(HISTORY_SETTINGS).getInteger(SIZE);
            }

            int breakLoop = 0;
            while (history.getJsonArray(DATA).size() > historySize && breakLoop < Historian.MAX_BREAK_LOOP) {
                history.getJsonArray(DATA).remove(0);
                breakLoop++;
            }

            db.getJsonObject(THING)
              .getJsonObject(FEATURES)
              .getJsonObject(HISTORIES)
              .getJsonObject(PROPERTIES)
              .put(id, history);
            return updateDittoData(db);
        }).doFinally(() -> syncHistoryLock.unlock());
    }

}
