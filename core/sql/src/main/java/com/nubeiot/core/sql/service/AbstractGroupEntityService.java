package com.nubeiot.core.sql.service;

import java.util.Map.Entry;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.GroupEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.query.GroupQueryExecutor;
import com.nubeiot.core.sql.service.marker.GroupReferencingEntityMarker;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code Group entity}.
 *
 * @param <M>  Type of {@code EntityMetadata}
 * @param <CP> Type of {@code CompositePojo}
 * @param <CM> Type of {@code CompositeMetadata}
 * @see VertxPojo
 * @see EntityMetadata
 * @see CompositePojo
 * @see CompositeMetadata
 * @see GroupEntityService
 * @see GroupEntityTransformer
 * @since 1.0.0
 */
public abstract class AbstractGroupEntityService<M extends EntityMetadata, CP extends CompositePojo,
                                                    CM extends CompositeMetadata>
    extends AbstractReferencingEntityService<CP, CM> implements GroupEntityService<M, CP, CM>, GroupEntityTransformer {

    /**
     * Instantiates a new Abstract group entity service.
     *
     * @param entityHandler the entity handler
     * @since 1.0.0
     */
    public AbstractGroupEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityValidation validation() {
        return this::rawContext;
    }

    @Override
    public @NonNull GroupQueryExecutor<CP> queryExecutor() {
        return GroupEntityService.super.queryExecutor();
    }

    @Override
    public @NonNull GroupEntityTransformer transformer() {
        return this;
    }

    @Override
    protected OperationValidator initCreationValidator() {
        return OperationValidator.create(
            (req, pojo) -> queryExecutor().checkReferenceExistence(req).map(b -> context().onCreating(req)));
    }

    @Override
    public GroupReferencingEntityMarker marker() {
        return this;
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return recomputeRequestData(requestData, convertKey(requestData, refFields()));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final JsonObject extra = convertKey(requestData, context());
        return recomputeRequestData(requestData, extra.mergeIn(convertKey(requestData, refFields()), true));
    }

    private Stream<Entry<EntityMetadata, String>> refFields() {
        return Stream.concat(marker().referencedEntities().getFields().entrySet().stream(),
                             marker().groupReferences().getFields().entrySet().stream());
    }

}
