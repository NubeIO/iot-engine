package com.nubeiot.core.sql.decorator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.Field;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero.utils.Strings;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.HasSyncAudit;
import com.nubeiot.core.sql.pojos.HasTimeAudit;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.type.SyncAudit;

import lombok.NonNull;

/**
 * Helper to add or update sync audit status
 *
 * @see HasSyncAudit
 * @see SyncAudit
 * @since 1.0.0
 */
public interface EntitySyncHandler extends EntityHandler {

    /**
     * Force not synced audit on creating if current entity is child of {@link HasSyncAudit}
     *
     * @param <P>  Pojo type
     * @param pojo Given entity
     * @return modified pojo for fluent API
     * @see SyncAudit#notYetSynced(String)
     * @since 1.0.0
     */
    static <P extends VertxPojo> P markNotSyncedOnCreating(@NonNull P pojo) {
        if (pojo instanceof HasSyncAudit) {
            ((HasSyncAudit) pojo).setSyncAudit(SyncAudit.notYetSynced("Not yet synced new resource"));
        }
        return pojo;
    }

    /**
     * Force not synced audit on creating if current entity is child of {@link HasSyncAudit}
     *
     * @param <P>  Type of {@code VertxPojo}
     * @param pojo Given entity
     * @return modified pojo for fluent API
     * @see SyncAudit#notYetSynced(SyncAudit, String)
     * @since 1.0.0
     */
    static <P extends VertxPojo> P markNotSyncedOnModified(@NonNull P pojo) {
        if (pojo instanceof HasSyncAudit) {
            final HasSyncAudit syncPojo = (HasSyncAudit) pojo;
            final SyncAudit prevSync = Optional.ofNullable(syncPojo.getSyncAudit()).orElse(SyncAudit.unknown());
            final Integer revision = pojo instanceof HasTimeAudit
                                     ? ((HasTimeAudit) pojo).getTimeAudit().getRevision()
                                     : null;
            final String message = Strings.format("Not yet synced modified resource{0}",
                                                  Objects.isNull(revision) ? "" : " with record revision " + revision);
            syncPojo.setSyncAudit(SyncAudit.notYetSynced(prevSync, message));
        }
        return pojo;
    }

    /**
     * Updates Sync success status.
     *
     * @param <P>      Type of {@code VertxPojo}
     * @param metadata the metadata
     * @param pojo     the pojo
     * @param response the response
     * @param by       the by
     * @return json result in maybe
     * @since 1.0.0
     */
    default <P extends VertxPojo> Maybe<JsonObject> syncSuccess(@NonNull EntityMetadata metadata, @NonNull P pojo,
                                                                JsonObject response, String by) {
        if (!(pojo instanceof HasSyncAudit)) {
            return Maybe.empty();
        }
        final SyncAudit audit = Optional.ofNullable(((HasSyncAudit) pojo).getSyncAudit()).orElse(SyncAudit.unknown());
        final SyncAudit syncAudit = SyncAudit.success(audit, response, by);
        return updateSyncedStatus(metadata, pojo, syncAudit);
    }

    /**
     * Updates Sync failed status.
     *
     * @param <P>      Type of {@code VertxPojo}
     * @param metadata the metadata
     * @param pojo     the pojo
     * @param t        the t
     * @param by       the by
     * @return json result in maybe
     * @since 1.0.0
     */
    default <P extends VertxPojo> Maybe<JsonObject> syncFailed(@NonNull EntityMetadata metadata, @NonNull P pojo,
                                                               @NonNull Throwable t, String by) {
        if (!(pojo instanceof HasSyncAudit)) {
            return Maybe.empty();
        }
        final SyncAudit audit = Optional.ofNullable(((HasSyncAudit) pojo).getSyncAudit()).orElse(SyncAudit.unknown());
        final SyncAudit syncAudit = SyncAudit.error(audit, ErrorMessage.parse(t).toJson(), by);
        return updateSyncedStatus(metadata, pojo, syncAudit);
    }

    /**
     * Updates synced status.
     *
     * @param <P>       Type of {@code VertxPojo}
     * @param metadata  the metadata
     * @param pojo      the pojo
     * @param syncAudit the sync audit
     * @return json result in maybe
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default <P extends VertxPojo> Maybe<JsonObject> updateSyncedStatus(@NonNull EntityMetadata metadata,
                                                                       @NonNull P pojo, @NonNull SyncAudit syncAudit) {
        final Object key = pojo.toJson().getValue(metadata.jsonKeyName());
        final Condition condition = metadata.table().getField(metadata.jsonKeyName()).eq(key);
        final HasSyncAudit updated = (HasSyncAudit) metadata.parseFromEntity(new JsonObject());
        final Map<Field, Object> sync = JsonPojo.from(updated.setSyncAudit(syncAudit))
                                                .toJson()
                                                .stream()
                                                .collect(Collectors.toMap(k -> metadata.table().getField(k.getKey()),
                                                                          Entry::getValue));
        return genericQuery().execute(dsl -> dsl.update(metadata.table()).set(sync).where(condition))
                             .map(r -> new JsonObject().put("action", EventAction.UPDATE)
                                                       .put("status", r > 0 ? Status.SUCCESS : Status.FAILED)
                                                       .put("record", r)
                                                       .put("key", key)
                                                       .put("resource", metadata.table().getName())
                                                       .put("sync_audit", syncAudit.toJson()))
                             .toMaybe();
    }

}
