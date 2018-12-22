/*
 * This file is generated by jOOQ.
*/
package com.nubeiot.edge.core.model.gen.tables.records;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Row11;
import org.jooq.impl.UpdatableRecordImpl;

import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.core.model.gen.tables.TblTransaction;
import com.nubeiot.edge.core.model.gen.tables.interfaces.ITblTransaction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
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
@Entity
@Table(name = "tbl_transaction", indexes = {
    @Index(name = "Idx_tbl_transaction_module_id", columnList = "module_id ASC"),
    @Index(name = "Idx_tbl_transaction_module_lifetime", columnList = "module_id ASC, issued_at ASC"),
    @Index(name = "sqlite_autoindex_tbl_transaction_1", unique = true, columnList = "transaction_id ASC")
})
public class TblTransactionRecord extends UpdatableRecordImpl<TblTransactionRecord> implements VertxPojo,
                                                                                               Record11<String,
                                                                                                               String
                                                                                                               , EventAction, Status, Date, String, String, Date, JsonObject, JsonObject, Integer>,
                                                                                               ITblTransaction {

    private static final long serialVersionUID = 415663572;

    /**
     * Setter for <code>tbl_transaction.transaction_id</code>.
     */
    @Override
    public TblTransactionRecord setTransactionId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.transaction_id</code>.
     */
    @Id
    @Column(name = "transaction_id", unique = true, nullable = false, length = 31)
    @Override
    public String getTransactionId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>tbl_transaction.module_id</code>.
     */
    @Override
    public TblTransactionRecord setModuleId(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.module_id</code>.
     */
    @Column(name = "module_id", nullable = false, length = 127)
    @Override
    public String getModuleId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>tbl_transaction.event</code>.
     */
    @Override
    public TblTransactionRecord setEvent(EventAction value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.event</code>.
     */
    @Column(name = "event", nullable = false, length = 15)
    @Override
    public EventAction getEvent() {
        return (EventAction) get(2);
    }

    /**
     * Setter for <code>tbl_transaction.status</code>.
     */
    @Override
    public TblTransactionRecord setStatus(Status value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.status</code>.
     */
    @Column(name = "status", nullable = false, length = 15)
    @Override
    public Status getStatus() {
        return (Status) get(3);
    }

    /**
     * Setter for <code>tbl_transaction.issued_at</code>.
     */
    @Override
    public TblTransactionRecord setIssuedAt(Date value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.issued_at</code>.
     */
    @Column(name = "issued_at", nullable = false)
    @Override
    public Date getIssuedAt() {
        return (Date) get(4);
    }

    /**
     * Setter for <code>tbl_transaction.issued_by</code>.
     */
    @Override
    public TblTransactionRecord setIssuedBy(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.issued_by</code>.
     */
    @Column(name = "issued_by", length = 127)
    @Override
    public String getIssuedBy() {
        return (String) get(5);
    }

    /**
     * Setter for <code>tbl_transaction.issued_from</code>.
     */
    @Override
    public TblTransactionRecord setIssuedFrom(String value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.issued_from</code>.
     */
    @Column(name = "issued_from", length = 63)
    @Override
    public String getIssuedFrom() {
        return (String) get(6);
    }

    /**
     * Setter for <code>tbl_transaction.modified_at</code>.
     */
    @Override
    public TblTransactionRecord setModifiedAt(Date value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.modified_at</code>.
     */
    @Column(name = "modified_at", nullable = false)
    @Override
    public Date getModifiedAt() {
        return (Date) get(7);
    }

    /**
     * Setter for <code>tbl_transaction.prev_state_json</code>.
     */
    @Override
    public TblTransactionRecord setPrevStateJson(JsonObject value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.prev_state_json</code>.
     */
    @Column(name = "prev_state_json")
    @Override
    public JsonObject getPrevStateJson() {
        return (JsonObject) get(8);
    }

    /**
     * Setter for <code>tbl_transaction.last_error_json</code>.
     */
    @Override
    public TblTransactionRecord setLastErrorJson(JsonObject value) {
        set(9, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.last_error_json</code>.
     */
    @Column(name = "last_error_json")
    @Override
    public JsonObject getLastErrorJson() {
        return (JsonObject) get(9);
    }

    /**
     * Setter for <code>tbl_transaction.retry</code>.
     */
    @Override
    public TblTransactionRecord setRetry(Integer value) {
        set(10, value);
        return this;
    }

    /**
     * Getter for <code>tbl_transaction.retry</code>.
     */
    @Column(name = "retry", nullable = false)
    @Override
    public Integer getRetry() {
        return (Integer) get(10);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record11 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row11<String, String, EventAction, Status, Date, String, String, Date, JsonObject, JsonObject, Integer> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row11<String, String, EventAction, Status, Date, String, String, Date, JsonObject, JsonObject, Integer> valuesRow() {
        return (Row11) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return TblTransaction.TBL_TRANSACTION.TRANSACTION_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return TblTransaction.TBL_TRANSACTION.MODULE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<EventAction> field3() {
        return TblTransaction.TBL_TRANSACTION.EVENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Status> field4() {
        return TblTransaction.TBL_TRANSACTION.STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Date> field5() {
        return TblTransaction.TBL_TRANSACTION.ISSUED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return TblTransaction.TBL_TRANSACTION.ISSUED_BY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return TblTransaction.TBL_TRANSACTION.ISSUED_FROM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Date> field8() {
        return TblTransaction.TBL_TRANSACTION.MODIFIED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<JsonObject> field9() {
        return TblTransaction.TBL_TRANSACTION.PREV_STATE_JSON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<JsonObject> field10() {
        return TblTransaction.TBL_TRANSACTION.LAST_ERROR_JSON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field11() {
        return TblTransaction.TBL_TRANSACTION.RETRY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getTransactionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getModuleId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventAction component3() {
        return getEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status component4() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date component5() {
        return getIssuedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getIssuedBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getIssuedFrom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date component8() {
        return getModifiedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject component9() {
        return getPrevStateJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject component10() {
        return getLastErrorJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component11() {
        return getRetry();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getTransactionId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getModuleId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventAction value3() {
        return getEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Status value4() {
        return getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date value5() {
        return getIssuedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getIssuedBy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getIssuedFrom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date value8() {
        return getModifiedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject value9() {
        return getPrevStateJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject value10() {
        return getLastErrorJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value11() {
        return getRetry();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value1(String value) {
        setTransactionId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value2(String value) {
        setModuleId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value3(EventAction value) {
        setEvent(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value4(Status value) {
        setStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value5(Date value) {
        setIssuedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value6(String value) {
        setIssuedBy(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value7(String value) {
        setIssuedFrom(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value8(Date value) {
        setModifiedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value9(JsonObject value) {
        setPrevStateJson(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value10(JsonObject value) {
        setLastErrorJson(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord value11(Integer value) {
        setRetry(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TblTransactionRecord values(String value1, String value2, EventAction value3, Status value4, Date value5,
                                       String value6, String value7, Date value8, JsonObject value9, JsonObject value10,
                                       Integer value11) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void from(ITblTransaction from) {
        setTransactionId(from.getTransactionId());
        setModuleId(from.getModuleId());
        setEvent(from.getEvent());
        setStatus(from.getStatus());
        setIssuedAt(from.getIssuedAt());
        setIssuedBy(from.getIssuedBy());
        setIssuedFrom(from.getIssuedFrom());
        setModifiedAt(from.getModifiedAt());
        setPrevStateJson(from.getPrevStateJson());
        setLastErrorJson(from.getLastErrorJson());
        setRetry(from.getRetry());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends ITblTransaction> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TblTransactionRecord
     */
    public TblTransactionRecord() {
        super(TblTransaction.TBL_TRANSACTION);
    }

    /**
     * Create a detached, initialised TblTransactionRecord
     */
    public TblTransactionRecord(String transactionId, String moduleId, EventAction event, Status status, Date issuedAt,
                                String issuedBy, String issuedFrom, Date modifiedAt, JsonObject prevStateJson,
                                JsonObject lastErrorJson, Integer retry) {
        super(TblTransaction.TBL_TRANSACTION);

        set(0, transactionId);
        set(1, moduleId);
        set(2, event);
        set(3, status);
        set(4, issuedAt);
        set(5, issuedBy);
        set(6, issuedFrom);
        set(7, modifiedAt);
        set(8, prevStateJson);
        set(9, lastErrorJson);
        set(10, retry);
    }

    public TblTransactionRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}
