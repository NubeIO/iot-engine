/*
 * This file is generated by jOOQ.
*/
package com.nubeio.iot.edge.model.gen.tables.interfaces;

import java.io.Serializable;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventType;

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
public interface ITblTransaction extends VertxPojo, Serializable {

    /**
     * Setter for <code>tbl_transaction.transaction_id</code>.
     */
    public ITblTransaction setTransactionId(String value);

    /**
     * Getter for <code>tbl_transaction.transaction_id</code>.
     */
    @Id
    @Column(name = "transaction_id", unique = true, nullable = false, length = 31)
    public String getTransactionId();

    /**
     * Setter for <code>tbl_transaction.module_id</code>.
     */
    public ITblTransaction setModuleId(String value);

    /**
     * Getter for <code>tbl_transaction.module_id</code>.
     */
    @Column(name = "module_id", nullable = false, length = 127)
    public String getModuleId();

    /**
     * Setter for <code>tbl_transaction.event</code>.
     */
    public ITblTransaction setEvent(EventType value);

    /**
     * Getter for <code>tbl_transaction.event</code>.
     */
    @Column(name = "event", nullable = false, length = 15)
    public EventType getEvent();

    /**
     * Setter for <code>tbl_transaction.status</code>.
     */
    public ITblTransaction setStatus(Status value);

    /**
     * Getter for <code>tbl_transaction.status</code>.
     */
    @Column(name = "status", nullable = false, length = 15)
    public Status getStatus();

    /**
     * Setter for <code>tbl_transaction.issued_at</code>.
     */
    public ITblTransaction setIssuedAt(Date value);

    /**
     * Getter for <code>tbl_transaction.issued_at</code>.
     */
    @Column(name = "issued_at", nullable = false)
    public Date getIssuedAt();

    /**
     * Setter for <code>tbl_transaction.issued_by</code>.
     */
    public ITblTransaction setIssuedBy(String value);

    /**
     * Getter for <code>tbl_transaction.issued_by</code>.
     */
    @Column(name = "issued_by", length = 127)
    public String getIssuedBy();

    /**
     * Setter for <code>tbl_transaction.issued_from</code>.
     */
    public ITblTransaction setIssuedFrom(String value);

    /**
     * Getter for <code>tbl_transaction.issued_from</code>.
     */
    @Column(name = "issued_from", length = 63)
    public String getIssuedFrom();

    /**
     * Setter for <code>tbl_transaction.modified_at</code>.
     */
    public ITblTransaction setModifiedAt(Date value);

    /**
     * Getter for <code>tbl_transaction.modified_at</code>.
     */
    @Column(name = "modified_at", nullable = false)
    public Date getModifiedAt();

    /**
     * Setter for <code>tbl_transaction.prev_state_json</code>.
     */
    public ITblTransaction setPrevStateJson(JsonObject value);

    /**
     * Getter for <code>tbl_transaction.prev_state_json</code>.
     */
    @Column(name = "prev_state_json")
    public JsonObject getPrevStateJson();

    /**
     * Setter for <code>tbl_transaction.last_error_json</code>.
     */
    public ITblTransaction setLastErrorJson(JsonObject value);

    /**
     * Getter for <code>tbl_transaction.last_error_json</code>.
     */
    @Column(name = "last_error_json")
    public JsonObject getLastErrorJson();

    /**
     * Setter for <code>tbl_transaction.retry</code>.
     */
    public ITblTransaction setRetry(Integer value);

    /**
     * Getter for <code>tbl_transaction.retry</code>.
     */
    @Column(name = "retry", nullable = false)
    public Integer getRetry();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface ITblTransaction
     */
    public void from(com.nubeio.iot.edge.model.gen.tables.interfaces.ITblTransaction from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface ITblTransaction
     */
    public <E extends com.nubeio.iot.edge.model.gen.tables.interfaces.ITblTransaction> E into(E into);

    @Override
    public default ITblTransaction fromJson(io.vertx.core.json.JsonObject json) {
        setTransactionId(json.getString("transaction_id"));
        setModuleId(json.getString("module_id"));
        setEvent(json.getString("event")==null?null:com.nubeio.iot.share.event.EventType.valueOf(json.getString("event")));
        setStatus(json.getString("status")==null?null:com.nubeio.iot.share.enums.Status.valueOf(json.getString("status")));
        setIssuedAt(json.getLong("issued_at")==null?null:Date.from(java.time.Instant.ofEpochMilli(json.getLong("issued_at"))));
        setIssuedBy(json.getString("issued_by"));
        setIssuedFrom(json.getString("issued_from"));
        setModifiedAt(json.getLong("modified_at")==null?null:Date.from(java.time.Instant.ofEpochMilli(json.getLong("modified_at"))));
        setPrevStateJson(json.getJsonObject("prev_state_json"));
        setLastErrorJson(json.getJsonObject("last_error_json"));
        setRetry(json.getInteger("retry"));
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("transaction_id",getTransactionId());
        json.put("module_id",getModuleId());
        json.put("event",getEvent()==null?null:getEvent());
        json.put("status",getStatus()==null?null:getStatus());
        json.put("issued_at",getIssuedAt()==null?null:getIssuedAt().getTime());
        json.put("issued_by",getIssuedBy());
        json.put("issued_from",getIssuedFrom());
        json.put("modified_at",getModifiedAt()==null?null:getModifiedAt().getTime());
        json.put("prev_state_json",getPrevStateJson());
        json.put("last_error_json",getLastErrorJson());
        json.put("retry",getRetry());
        return json;
    }

}
