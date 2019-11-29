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
import com.nubeiot.core.sql.service.workflow.CreationStep;
import com.nubeiot.core.sql.service.workflow.DeletionStep;
import com.nubeiot.core.sql.service.workflow.ModificationStep;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

public abstract class AbstractGroupEntityService<P extends VertxPojo, M extends EntityMetadata,
                                                    CP extends CompositePojo<P, CP>, CM extends CompositeMetadata>
    extends AbstractOneToManyEntityService<P, M>
    implements GroupEntityService<P, M, CP, CM>, GroupEntityTransformer, GroupReferenceResource {

    public AbstractGroupEntityService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull GroupEntityTransformer transformer() {
        return this;
    }

    @Override
    protected OperationValidator getCreationValidator() {
        return OperationValidator.create(
            (req, pojo) -> groupQuery().mustExists(req, ref()).map(b -> contextGroup().onCreating(req)));
    }

    @Override
    public GroupReferenceResource ref() {
        return this;
    }

    @Override
    public @NonNull RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        final Stream<Entry<EntityMetadata, String>> stream = Stream.of(
            ref().entityReferences().getFields().entrySet().stream(),
            ref().groupReferences().getFields().entrySet().stream()).flatMap(s -> s);
        return recomputeRequestData(requestData, convertKey(requestData, stream));
    }

    @Override
    public @NonNull RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        final JsonObject extra = convertKey(requestData, context());
        final Stream<Entry<EntityMetadata, String>> stream = Stream.of(
            ref().entityReferences().getFields().entrySet().stream(),
            ref().groupReferences().getFields().entrySet().stream()).flatMap(s -> s);
        return recomputeRequestData(requestData, extra.mergeIn(convertKey(requestData, stream), true));
    }

    @Override
    protected CreationStep getCreationStep() {
        return CreationStep.builder().queryExecutor(groupQuery()).build();
    }

    protected ModificationStep getModificationStep(EventAction action) {
        return ModificationStep.builder().action(action).queryExecutor(groupQuery()).build();
    }

    @Override
    protected DeletionStep getDeletionStep() {
        return DeletionStep.builder().queryExecutor(groupQuery()).build();
    }

}
