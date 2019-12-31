package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.query.SimpleQueryExecutor;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.DefaultDMLWorkflow;
import com.nubeiot.core.sql.workflow.DefaultDQLBatchWorkflow;
import com.nubeiot.core.sql.workflow.DefaultDQLWorkflow;
import com.nubeiot.core.sql.workflow.step.CreationStep;
import com.nubeiot.core.sql.workflow.step.DeletionStep;
import com.nubeiot.core.sql.workflow.step.GetManyStep;
import com.nubeiot.core.sql.workflow.step.GetOneStep;
import com.nubeiot.core.sql.workflow.step.ModificationStep;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstract service to implement {@code CRUD} listeners for {@code database entity}
 *
 * @param <P> Type of {@code VertxPojo}
 * @param <M> Type of {@code EntityMetadata}
 * @see EntityService
 * @see EntityTransformer
 * @since 1.0.0
 */
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public abstract class AbstractEntityService<P extends VertxPojo, M extends EntityMetadata>
    implements SimpleEntityService<P, M>, EntityTransformer {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final EntityHandler entityHandler;

    @Override
    public EntityHandler entityHandler() { return entityHandler; }

    @Override
    public @NonNull SimpleQueryExecutor<P> queryExecutor() {
        return SimpleQueryExecutor.create(entityHandler(), context());
    }

    @Override
    public @NonNull EntityValidation validation() { return context(); }

    @Override
    public @NonNull EntityTransformer transformer() { return this; }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        return DefaultDQLBatchWorkflow.builder()
                                      .action(EventAction.GET_LIST)
                                      .metadata(context())
                                      .normalize(this::onReadingManyResource)
                                      .validator(initGetOneValidator())
                                      .preTask(prePersistTask())
                                      .sqlStep(initGetManyStep())
                                      .postTask(postPersistTask())
                                      .asyncPostTask(postPersistAsyncTask())
                                      .transformer((req, res) -> transformer().onMany(res))
                                      .build()
                                      .run(requestData);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        return DefaultDQLWorkflow.<P>builder().action(EventAction.GET_ONE)
                                              .metadata(context())
                                              .normalize(this::onReadingOneResource)
                                              .validator(initGetOneValidator())
                                              .preTask(prePersistTask())
                                              .sqlStep(initGetOneStep())
                                              .postTask(postPersistTask())
                                              .asyncPostTask(postPersistAsyncTask())
                                              .transformer((req, res) -> transformer().afterGet(res, req))
                                              .build()
                                              .run(requestData);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.CREATE)
                                 .metadata(context())
                                 .normalize(this::onCreatingOneResource)
                                 .validator(initCreationValidator())
                                 .preTask(prePersistTask())
                                 .sqlStep(initCreationStep())
                                 .postTask(postPersistTask())
                                 .asyncPostTask(postPersistAsyncTask())
                                 .transformer((r, p) -> transformer().afterCreate(p.primaryKey(), p.dbEntity(), r))
                                 .build()
                                 .run(requestData);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.UPDATE)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(initUpdateValidator())
                                 .preTask(prePersistTask())
                                 .sqlStep(initModificationStep(EventAction.UPDATE))
                                 .postTask(postPersistTask())
                                 .asyncPostTask(postPersistAsyncTask())
                                 .transformer((r, p) -> transformer().afterUpdate(p.primaryKey(), p.dbEntity(), r))
                                 .build()
                                 .run(requestData);
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.PATCH)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(initPatchValidator())
                                 .preTask(prePersistTask())
                                 .sqlStep(initModificationStep(EventAction.PATCH))
                                 .postTask(postPersistTask())
                                 .asyncPostTask(postPersistAsyncTask())
                                 .transformer((r, p) -> transformer().afterPatch(p.primaryKey(), p.dbEntity(), r))
                                 .build()
                                 .run(requestData);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.REMOVE)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(initDeletionValidator())
                                 .preTask(prePersistTask())
                                 .sqlStep(initDeletionStep())
                                 .postTask(postPersistTask())
                                 .asyncPostTask(postPersistAsyncTask())
                                 .transformer((req, re) -> transformer().afterDelete(re.dbEntity(), req))
                                 .build()
                                 .run(requestData);
    }

    @Override
    public @NonNull EntityMetadata resourceMetadata() {
        return context();
    }

    @NonNull
    protected GetManyStep initGetManyStep() {
        return GetManyStep.builder()
                          .action(EventAction.GET_LIST)
                          .queryExecutor(queryExecutor())
                          .onEach(transformer()::onEach)
                          .build();
    }

    @NonNull
    protected OperationValidator initGetOneValidator() {
        return OperationValidator.create((req, pojo) -> Single.just(pojo));
    }

    @NonNull
    protected <PP extends P> GetOneStep<PP> initGetOneStep() {
        return GetOneStep.<PP>builder().action(EventAction.GET_ONE).queryExecutor(queryExecutor()).build();
    }

    @NonNull
    protected OperationValidator initCreationValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onCreating(req)));
    }

    @NonNull
    protected CreationStep initCreationStep() {
        return CreationStep.builder().action(EventAction.CREATE).queryExecutor(queryExecutor()).build();
    }

    @NonNull
    protected OperationValidator initPatchValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onPatching(dbEntity, req)));
    }

    @NonNull
    protected OperationValidator initUpdateValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onUpdating(dbEntity, req)));
    }

    @NonNull
    protected ModificationStep initModificationStep(@NonNull EventAction action) {
        return ModificationStep.builder().action(action).queryExecutor(queryExecutor()).build();
    }

    @NonNull
    protected OperationValidator initDeletionValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onDeleting(dbEntity, req)));
    }

    @NonNull
    protected DeletionStep initDeletionStep() {
        return DeletionStep.builder().action(EventAction.REMOVE).queryExecutor(queryExecutor()).build();
    }

}
