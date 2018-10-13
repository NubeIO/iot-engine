/*
 * This file is generated by jOOQ.
*/
package com.nubeio.iot.edge.model.gen.tables.daos;


import com.nubeio.iot.edge.model.gen.tables.TblTransaction;
import com.nubeio.iot.edge.model.gen.tables.records.TblTransactionRecord;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventType;

import io.github.jklingsporn.vertx.jooq.shared.internal.AbstractVertxDAO;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Configuration;


import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Optional;
import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXQueryExecutor;
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
public class TblTransactionDao extends AbstractVertxDAO<TblTransactionRecord, com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction, String, Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>>, Single<Optional<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>>, Single<Integer>, Single<String>> implements io.github.jklingsporn.vertx.jooq.rx.VertxDAO<TblTransactionRecord,com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction,String> {

    /**
     * @param configuration The Configuration used for rendering and query execution.
     * @param vertx the vertx instance
     */
    public TblTransactionDao(Configuration configuration, io.vertx.reactivex.core.Vertx vertx) {
        super(TblTransaction.TBL_TRANSACTION, com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction.class, new JDBCRXQueryExecutor<TblTransactionRecord,com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction,String>(com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction.class,configuration,vertx), configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getId(com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction object) {
        return object.getTransactionId();
    }

    /**
     * Find records that have <code>module_id IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByModuleId(List<String> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.MODULE_ID.in(values));
    }

    /**
     * Find records that have <code>event IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByEvent(List<EventType> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.EVENT.in(values));
    }

    /**
     * Find records that have <code>status IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByStatus(List<Status> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.STATUS.in(values));
    }

    /**
     * Find records that have <code>issued_at IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByIssuedAt(List<Date> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.ISSUED_AT.in(values));
    }

    /**
     * Find records that have <code>issued_by IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByIssuedBy(List<String> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.ISSUED_BY.in(values));
    }

    /**
     * Find records that have <code>issued_from IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByIssuedFrom(List<String> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.ISSUED_FROM.in(values));
    }

    /**
     * Find records that have <code>modified_at IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByModifiedAt(List<Date> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.MODIFIED_AT.in(values));
    }

    /**
     * Find records that have <code>prev_state_json IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByPrevStateJson(List<JsonObject> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.PREV_STATE_JSON.in(values));
    }

    /**
     * Find records that have <code>last_error_json IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByLastErrorJson(List<JsonObject> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.LAST_ERROR_JSON.in(values));
    }

    /**
     * Find records that have <code>retry IN (values)</code> asynchronously
     */
    public Single<List<com.nubeio.iot.edge.model.gen.tables.pojos.TblTransaction>> findManyByRetry(List<Integer> values) {
        return findManyByCondition(TblTransaction.TBL_TRANSACTION.RETRY.in(values));
    }
}
