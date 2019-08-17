package com.nubeiot.core.sql.decorator;

import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.pojos.HasSyncAudit;
import com.nubeiot.core.sql.pojos.HasTimeAudit;
import com.nubeiot.core.sql.type.SyncAudit;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Decorator {@code entity} with {@code audit} properties before persist in database
 *
 * @see VertxPojo
 * @see HasSyncAudit
 * @see HasTimeAudit
 */
//TODO MOVE to DAO internal
public interface EntityAuditDecorator {

    static <P extends VertxPojo> P addCreationAudit(boolean auditEnabled, @NonNull P pojo, String createdBy) {
        return addCreationTimeAudit(auditEnabled, addNotSyncAudit(pojo), createdBy);
    }

    static <P extends VertxPojo> P addModifiedAudit(boolean auditEnabled, @NonNull P dbData, @NonNull P pojo,
                                                    String modifiedBy) {
        return addModifiedTimeAudit(auditEnabled, dbData, addNotSyncAudit(pojo), modifiedBy);
    }

    /**
     * Add creation time audit if current entity is child of {@link HasTimeAudit}
     *
     * @param auditEnabled Whether enable audit or not
     * @param pojo         Given entity
     * @param createdBy    Created by
     * @return modified pojo for fluent API
     */
    static <M extends VertxPojo> M addCreationTimeAudit(boolean auditEnabled, @NonNull M pojo, String createdBy) {
        if (auditEnabled && pojo instanceof HasTimeAudit) {
            ((HasTimeAudit) pojo).setTimeAudit(TimeAudit.created(Strings.fallback(createdBy, "UNKNOWN")));
        }
        return pojo;
    }

    /**
     * Add modified time audit if current entity is child of {@link HasTimeAudit}
     *
     * @param auditEnabled Whether enable audit or not
     * @param dbData       Existed entity in database
     * @param pojo         Given entity
     * @param modifiedBy   Modified by
     * @return modified pojo for fluent API
     */
    static <M extends VertxPojo> M addModifiedTimeAudit(boolean auditEnabled, @NonNull M dbData, @NonNull M pojo,
                                                        String modifiedBy) {
        if (auditEnabled && pojo instanceof HasTimeAudit) {
            ((HasTimeAudit) pojo).setTimeAudit(
                TimeAudit.modified(((HasTimeAudit) dbData).getTimeAudit(), Strings.fallback(modifiedBy, "UNKNOWN")));
        }
        return pojo;
    }

    /**
     * Force not synced audit if current entity is child of {@link HasSyncAudit}
     *
     * @param pojo Given entity
     * @return modified pojo for fluent API
     * @apiNote It is exclude audit if entity has already had {@code SyncAudit}
     * @see SyncAudit#notSynced()
     */
    static <M extends VertxPojo> M addNotSyncAudit(@NonNull M pojo) {
        if (pojo instanceof HasSyncAudit) {
            ((HasSyncAudit) pojo).setSyncAudit(
                Optional.ofNullable(((HasSyncAudit) pojo).getSyncAudit()).orElseGet(SyncAudit::notSynced));
        }
        return pojo;
    }

    /**
     * Force synced audit if current entity is child of {@link HasSyncAudit}
     *
     * @param pojo Given entity
     * @return modified pojo for fluent API
     * @apiNote It is exclude audit if entity has already had {@code SyncAudit}
     * @see SyncAudit#synced()
     */
    static <M extends VertxPojo> M addSyncAudit(@NonNull M pojo) {
        if (pojo instanceof HasSyncAudit) {
            ((HasSyncAudit) pojo).setSyncAudit(
                Optional.ofNullable(((HasSyncAudit) pojo).getSyncAudit()).orElseGet(SyncAudit::synced));
        }
        return pojo;
    }

}
