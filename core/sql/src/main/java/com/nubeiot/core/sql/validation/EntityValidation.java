package com.nubeiot.core.sql.validation;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityAuditDecorator;
import com.nubeiot.core.sql.pojos.JsonPojo;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface EntityValidation<P extends VertxPojo> {

    AbstractEntityHandler entityHandler();

    EntityMetadata metadata();

    /**
     * Validate when creating new resource. Any {@code override} method for custom validation by each object should be
     * ended by recall {@code super} to implement {@code time audit}
     *
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    default P onCreate(@NonNull P pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
        return EntityAuditDecorator.addCreationAudit(metadata().enableTimeAudit(), pojo,
                                                     headers.getString(Headers.X_REQUEST_USER, null));
    }

    /**
     * Validate when updating resource. Any {@code override} method for custom validation by each object should be ended
     * by recall {@code super} to implement {@code time audit}
     *
     * @param dbData  existing resource object from database
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    default P onUpdate(@NonNull P dbData, @NonNull P pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return EntityAuditDecorator.addModifiedAudit(metadata().enableTimeAudit(), dbData, pojo,
                                                     headers.getString(Headers.X_REQUEST_USER, null));
    }

    /**
     * Validate when patching resource. Any {@code override} method for custom validation by each object should be ended
     * by recall {@code super} to implement {@code time audit}
     *
     * @param dbData  existing resource object from database
     * @param pojo    given request resource object
     * @param headers request header. It is useful to audit
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    @SuppressWarnings("unchecked")
    default P onPatch(@NonNull P dbData, @NonNull P pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
        return EntityAuditDecorator.addModifiedAudit(metadata().enableTimeAudit(), dbData,
                                                     (P) metadata().parse(JsonPojo.merge(dbData, pojo)),
                                                     headers.getString(Headers.X_REQUEST_USER, null));
    }

    default P onDelete(@NonNull P pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
        return pojo;
    }

    @RequiredArgsConstructor
    class DefaultEntityValidation implements EntityValidation {

        private final AbstractEntityHandler handler;
        private final EntityMetadata metadata;

        @Override
        public AbstractEntityHandler entityHandler() {
            return handler;
        }

        @Override
        public EntityMetadata metadata() {
            return metadata;
        }

    }

}
