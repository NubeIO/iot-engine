package com.nubeiot.core.sql.decorator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.Field;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
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
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Helper to add or update sync audit status
 *
 * @see HasSyncAudit
 * @see SyncAudit
 */
public interface EntitySyncHandler extends EntityHandler {

    /**
     * Force not synced audit on creating if current entity is child of {@link HasSyncAudit}
     *
     * @param pojo Given entity
     * @param <P>  Pojo type
     * @return modified pojo for fluent API
     * @see SyncAudit#notYetSynced(String)
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
     * @param pojo Given entity
     * @param <P>  Pojo type
     * @return modified pojo for fluent API
     * @see SyncAudit#notYetSynced(SyncAudit, String)
     */
    static <P extends VertxPojo> P markNotSyncedOnModified(@NonNull P pojo) {
        if (pojo instanceof HasSyncAudit) {
            final HasSyncAudit syncPojo = (HasSyncAudit) pojo;
            final SyncAudit prevSync = Optional.ofNullable(syncPojo.getSyncAudit()).orElse(SyncAudit.unknown());
            final Integer recVer = pojo instanceof HasTimeAudit ? ((HasTimeAudit) pojo).getTimeAudit()
                                                                                       .getRecordVersion() : null;
            final String message = Strings.format("Not yet synced modified resource{0}",
                                                  Objects.isNull(recVer) ? "" : " with record version " + recVer);
            syncPojo.setSyncAudit(SyncAudit.notYetSynced(prevSync, message));
        }
        return pojo;
    }

    default <P extends VertxPojo> Maybe<JsonObject> syncSuccess(@NonNull EntityMetadata metadata, @NonNull P pojo,
                                                                JsonObject response, String by) {
        if (!(pojo instanceof HasSyncAudit)) {
            return Maybe.empty();
        }
        final SyncAudit audit = Optional.ofNullable(((HasSyncAudit) pojo).getSyncAudit()).orElse(SyncAudit.unknown());
        final SyncAudit syncAudit = SyncAudit.success(audit, response, by);
        return updateSyncedStatus(metadata, pojo, syncAudit);
    }

    default <P extends VertxPojo> Maybe<JsonObject> syncFailed(@NonNull EntityMetadata metadata, @NonNull P pojo,
                                                               @NonNull Throwable t, String by) {
        if (!(pojo instanceof HasSyncAudit)) {
            return Maybe.empty();
        }
        final SyncAudit syncAudit = SyncAudit.error(((HasSyncAudit) pojo).getSyncAudit(),
                                                    ErrorMessage.parse(t).toJson(), by);
        return updateSyncedStatus(metadata, pojo, syncAudit);
    }

    @SuppressWarnings("unchecked")
    default <P extends VertxPojo> Maybe<JsonObject> updateSyncedStatus(@NonNull EntityMetadata metadata,
                                                                       @NonNull P pojo, @NonNull SyncAudit syncAudit) {
        final Object key = pojo.toJson().getValue(metadata.jsonKeyName());
        final Condition condition = metadata.table().getField(metadata.jsonKeyName()).eq(key);
        final HasSyncAudit updated = (HasSyncAudit) metadata.parseFromRequest(new JsonObject());
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
