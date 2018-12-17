/*
 * This file is generated by jOOQ.
*/
package com.nubeiot.edge.core.model.gen.tables.daos;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Generated;

import org.jooq.Configuration;

import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventType;
import com.nubeiot.edge.core.model.gen.tables.TblRemoveHistory;
import com.nubeiot.edge.core.model.gen.tables.records.TblRemoveHistoryRecord;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.AbstractVertxDAO;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.8"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TblRemoveHistoryDao extends AbstractVertxDAO<TblRemoveHistoryRecord, com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory, String, Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>>, Single<Optional<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>>, Single<Integer>, Single<String>> implements io.github.jklingsporn.vertx.jooq.rx.VertxDAO<TblRemoveHistoryRecord, com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory,String> {

    /**
     * @param configuration The Configuration used for rendering and query execution.
     * @param vertx the vertx instance
     */
    public TblRemoveHistoryDao(Configuration configuration, io.vertx.reactivex.core.Vertx vertx) {
        super(TblRemoveHistory.TBL_REMOVE_HISTORY, com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory.class, new JDBCRXQueryExecutor<TblRemoveHistoryRecord, com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory,String>(
                com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory.class, configuration, vertx), configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getId(com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory object) {
        return object.getTransactionId();
    }

    /**
     * Find records that have <code>module_id IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByModuleId(List<String> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.MODULE_ID.in(values));
    }

    /**
     * Find records that have <code>event IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByEvent(List<EventType> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.EVENT.in(values));
    }

    /**
     * Find records that have <code>status IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByStatus(List<Status> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.STATUS.in(values));
    }

    /**
     * Find records that have <code>issued_at IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByIssuedAt(List<Date> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.ISSUED_AT.in(values));
    }

    /**
     * Find records that have <code>issued_by IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByIssuedBy(List<String> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.ISSUED_BY.in(values));
    }

    /**
     * Find records that have <code>issued_from IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByIssuedFrom(List<String> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.ISSUED_FROM.in(values));
    }

    /**
     * Find records that have <code>modified_at IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByModifiedAt(List<Date> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.MODIFIED_AT.in(values));
    }

    /**
     * Find records that have <code>prev_state_json IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByPrevStateJson(List<JsonObject> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.PREV_STATE_JSON.in(values));
    }

    /**
     * Find records that have <code>last_error_json IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByLastErrorJson(List<JsonObject> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.LAST_ERROR_JSON.in(values));
    }

    /**
     * Find records that have <code>retry IN (events)</code> asynchronously
     */
    public Single<List<com.nubeiot.edge.core.model.gen.tables.pojos.TblRemoveHistory>> findManyByRetry(List<Integer> values) {
        return findManyByCondition(TblRemoveHistory.TBL_REMOVE_HISTORY.RETRY.in(values));
    }
}
