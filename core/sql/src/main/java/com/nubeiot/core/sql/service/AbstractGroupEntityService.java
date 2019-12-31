package com.nubeiot.core.sql.service;

import java.util.Map.Entry;
import java.util.stream.Stream;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.GroupEntityTransformer;
import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.core.sql.service.marker.GroupReferencingEntityMarker;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.CreationStep;
import com.nubeiot.core.sql.workflow.step.DeletionStep;
import com.nubeiot.core.sql.workflow.step.GetOneStep;
import com.nubeiot.core.sql.workflow.step.ModificationStep;

import lombok.NonNull;

/**
 * Abstract service to implement {@code CRUD} listeners for the {@code Group entity}.
 *
 * @param <P>  Type of {@code VertxPojo}
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
public abstract class AbstractGroupEntityService<P extends VertxPojo, M extends EntityMetadata,
                                                    CP extends CompositePojo<P, CP>, CM extends CompositeMetadata>
    extends AbstractReferencingEntityService<P, M> implements GroupEntityService<P, M, CP, CM>, GroupEntityTransformer {

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
    public @NonNull GroupEntityTransformer transformer() {
        return this;
    }

    @Override
    protected OperationValidator initCreationValidator() {
        return OperationValidator.create(
            (req, pojo) -> groupQuery().checkReferenceExistence(req).map(b -> groupContext().onCreating(req)));
    }

    @Override
    public GroupReferencingEntityMarker marker() {
        return this;
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        final Stream<Entry<EntityMetadata, String>> stream = Stream.of(
            marker().referencedEntities().getFields().entrySet().stream(),
            marker().groupReferences().getFields().entrySet().stream()).flatMap(s -> s);
        return recomputeRequestData(requestData, convertKey(requestData, stream));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final JsonObject extra = convertKey(requestData, context());
        final Stream<Entry<EntityMetadata, String>> stream = Stream.of(
            marker().referencedEntities().getFields().entrySet().stream(),
            marker().groupReferences().getFields().entrySet().stream()).flatMap(s -> s);
        return recomputeRequestData(requestData, extra.mergeIn(convertKey(requestData, stream), true));
    }

    @SuppressWarnings("unchecked")
    protected GetOneStep<CP> initGetOneStep() {
        return GetOneStep.<CP>builder().action(EventAction.GET_ONE).queryExecutor(groupQuery()).build();
    }

    @Override
    protected CreationStep initCreationStep() {
        return CreationStep.builder().action(EventAction.CREATE).queryExecutor(groupQuery()).build();
    }

    protected ModificationStep initModificationStep(EventAction action) {
        return ModificationStep.builder().action(action).queryExecutor(groupQuery()).build();
    }

    @Override
    protected DeletionStep initDeletionStep() {
        return DeletionStep.builder().action(EventAction.REMOVE).queryExecutor(groupQuery()).build();
    }

}
