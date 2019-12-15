package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
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
import com.nubeiot.core.sql.service.workflow.CreationStep;
import com.nubeiot.core.sql.service.workflow.DefaultDMLWorkflow;
import com.nubeiot.core.sql.service.workflow.DefaultDQLWorkflow;
import com.nubeiot.core.sql.service.workflow.DeletionStep;
import com.nubeiot.core.sql.service.workflow.EntityTaskWorkflow.AsyncEntityTaskWorkflow;
import com.nubeiot.core.sql.service.workflow.EntityTaskWorkflow.BlockingEntityTaskWorkflow;
import com.nubeiot.core.sql.service.workflow.GetManyStep;
import com.nubeiot.core.sql.service.workflow.GetOneStep;
import com.nubeiot.core.sql.service.workflow.ModificationStep;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.sql.validation.OperationValidator;

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
    @SuppressWarnings("unchecked")
    public @NonNull SimpleQueryExecutor<P> queryExecutor() {
        return SimpleQueryExecutor.create(entityHandler(), context());
    }

    @Override
    public @NonNull EntityValidation validation() { return context(); }

    @Override
    public @NonNull EntityTransformer transformer() { return this; }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        return DefaultDQLWorkflow.<JsonArray>builder().action(EventAction.GET_LIST)
                                                      .metadata(context())
                                                      .normalize(this::onReadingManyResource)
                                                      .validator(initGetOneValidator())
                                                      .prePersist(initPrePersistWorkflow())
                                                      .query(initGetManyStep())
                                                      .postAsyncPersist(initPostAsyncPersistWorkflow())
                                                      .transformer((req, res) -> transformer().onMany(res))
                                                      .build()
                                                      .execute(requestData);
    }

    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        return DefaultDQLWorkflow.<P>builder().action(EventAction.GET_ONE)
                                              .metadata(context())
                                              .normalize(this::onReadingOneResource)
                                              .validator(initGetOneValidator())
                                              .prePersist(initPrePersistWorkflow())
                                              .query(initGetOneStep())
                                              .postAsyncPersist(initPostAsyncPersistWorkflow())
                                              .transformer((req, res) -> transformer().afterGet(res, req))
                                              .build()
                                              .execute(requestData);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.CREATE)
                                 .metadata(context())
                                 .normalize(this::onCreatingOneResource)
                                 .validator(initCreationValidator())
                                 .prePersist(initPrePersistWorkflow())
                                 .persist(initCreationStep())
                                 .postAsyncPersist(initPostAsyncPersistWorkflow())
                                 .transformer((req, res) -> transformer().afterCreate(res.key(), res.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.UPDATE)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(initUpdateValidator())
                                 .prePersist(initPrePersistWorkflow())
                                 .persist(initModificationStep(EventAction.UPDATE))
                                 .postAsyncPersist(initPostAsyncPersistWorkflow())
                                 .transformer((req, res) -> transformer().afterUpdate(res.key(), res.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.PATCH)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(initPatchValidator())
                                 .prePersist(initPrePersistWorkflow())
                                 .persist(initModificationStep(EventAction.PATCH))
                                 .postAsyncPersist(initPostAsyncPersistWorkflow())
                                 .transformer((req, re) -> transformer().afterPatch(re.key(), re.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.REMOVE)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(initDeletionValidator())
                                 .prePersist(initPrePersistWorkflow())
                                 .persist(initDeletionStep())
                                 .postAsyncPersist(initPostAsyncPersistWorkflow())
                                 .transformer((req, re) -> transformer().afterDelete(re.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    @Override
    public @NonNull EntityMetadata resourceMetadata() {
        return context();
    }

    protected BlockingEntityTaskWorkflow initPrePersistWorkflow() {
        return BlockingEntityTaskWorkflow.create(prePersistTask().orElse(null));
    }

    protected AsyncEntityTaskWorkflow initPostAsyncPersistWorkflow() {
        return AsyncEntityTaskWorkflow.create(postPersistAsyncTask().orElse(null));
    }

    protected GetManyStep initGetManyStep() {
        return GetManyStep.builder()
                          .action(EventAction.GET_LIST)
                          .queryExecutor(queryExecutor())
                          .transformFunction(transformer()::onEach)
                          .build();
    }

    protected OperationValidator initGetOneValidator() {
        return OperationValidator.create((req, pojo) -> Single.just(pojo));
    }

    protected <PP extends P> GetOneStep<PP> initGetOneStep() {
        return GetOneStep.<PP>builder().action(EventAction.GET_ONE).queryExecutor(queryExecutor()).build();
    }

    protected OperationValidator initCreationValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onCreating(req)));
    }

    protected CreationStep initCreationStep() {
        return CreationStep.builder().action(EventAction.CREATE).queryExecutor(queryExecutor()).build();
    }

    protected OperationValidator initPatchValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onPatching(dbEntity, req)));
    }

    protected OperationValidator initUpdateValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onUpdating(dbEntity, req)));
    }

    protected ModificationStep initModificationStep(@NonNull EventAction action) {
        return ModificationStep.builder().action(action).queryExecutor(queryExecutor()).build();
    }

    protected OperationValidator initDeletionValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onDeleting(dbEntity, req)));
    }

    protected DeletionStep initDeletionStep() {
        return DeletionStep.builder().action(EventAction.REMOVE).queryExecutor(queryExecutor()).build();
    }

}
