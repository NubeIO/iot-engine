package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.ReferenceEntityTransformer;
import com.nubeiot.core.sql.query.ReferenceQueryExecutor;
import com.nubeiot.core.sql.service.marker.ReferencingEntityMarker;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code one-to-many entity}.
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see OneToManyEntityService
 * @see ReferenceEntityTransformer
 * @since 1.0.0
 */
public abstract class AbstractOneToManyEntityService<P extends VertxPojo, M extends EntityMetadata>
    extends HasReferenceEntityService<P, M> implements OneToManyEntityService<P, M>, ReferenceEntityTransformer {

    /**
     * Instantiates a new Abstract one to many entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
    public AbstractOneToManyEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull ReferenceQueryExecutor<P> queryExecutor() {
        return OneToManyEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull ReferenceEntityTransformer transformer() {
        return this;
    }

    protected OperationValidator initCreationValidator() {
        return OperationValidator.create(
            (req, pojo) -> queryExecutor().checkReferenceExistence(req).map(b -> validation().onCreating(req)));
    }

    @Override
    public ReferencingEntityMarker marker() {
        return this;
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, marker().referencedEntities()
                                                                                 .getFields()
                                                                                 .entrySet()
                                                                                 .stream()));
    }

    @Override
    @NonNull
    public RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final JsonObject extra = convertKey(requestData, context());
        final JsonObject refExtra = convertKey(requestData,
                                               marker().referencedEntities().getFields().entrySet().stream());
        return recomputeRequestData(requestData, extra.mergeIn(refExtra, true));
    }

    @Override
    @NonNull
    public RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, null);
    }

    @Override
    @NonNull
    public RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, context()));
    }

}
