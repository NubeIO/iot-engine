package com.nubeiot.core.sql.validation;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.sql.decorator.EntityAuditDecorator;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.pojos.JsonPojo;

import lombok.NonNull;

public interface CompositeValidation<POJO extends VertxPojo, CP extends CompositePojo<POJO, CP>>
    extends EntityValidation<CP> {

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
    default CP onCreate(@NonNull CP pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
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
    default CP onUpdate(@NonNull CP dbData, @NonNull CP pojo, @NonNull JsonObject headers)
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
    default CP onPatch(@NonNull CP dbData, @NonNull CP pojo, @NonNull JsonObject headers)
        throws IllegalArgumentException {
        return EntityAuditDecorator.addModifiedAudit(metadata().enableTimeAudit(), dbData,
                                                     (CP) metadata().parse(JsonPojo.merge(dbData, pojo)),
                                                     headers.getString(Headers.X_REQUEST_USER, null));
    }

    default CP onDelete(@NonNull CP pojo, @NonNull JsonObject headers) throws IllegalArgumentException {
        return pojo;
    }

}
