package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
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
import com.nubeiot.core.sql.service.workflow.DeletionStep;
import com.nubeiot.core.sql.service.workflow.ModificationStep;
import com.nubeiot.core.sql.service.workflow.TaskWorkflow.AsyncTaskWorkflow;
import com.nubeiot.core.sql.service.workflow.TaskWorkflow.BlockingTaskWorkflow;
import com.nubeiot.core.sql.validation.EntityValidation;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Abstract service to implement {@code CRUD} listeners for entity
 */
@RequiredArgsConstructor
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

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        RequestData reqData = onReadingManyResource(requestData);
        return doGetMany(reqData).flatMapSingle(m -> transformer().afterEachList(m, reqData))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(transformer()::wrapListData);
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        RequestData reqData = onReadingOneResource(requestData);
        return doGetOne(reqData).flatMap(pojo -> transformer().afterGet(pojo, reqData));
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.CREATE)
                                 .metadata(context())
                                 .normalize(this::onCreatingOneResource)
                                 .validator(getCreationValidator())
                                 .prePersist(BlockingTaskWorkflow.create(prePersistTask().orElse(null)))
                                 .persist(getCreationStep())
                                 .postAsyncPersist(AsyncTaskWorkflow.create(postPersistAsyncTask().orElse(null)))
                                 .transformer((req, res) -> transformer().afterCreate(res.key(), res.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.UPDATE)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(getUpdateValidator())
                                 .prePersist(BlockingTaskWorkflow.create(prePersistTask().orElse(null)))
                                 .persist(getModificationStep(EventAction.UPDATE))
                                 .postAsyncPersist(AsyncTaskWorkflow.create(postPersistAsyncTask().orElse(null)))
                                 .transformer((req, res) -> transformer().afterUpdate(res.key(), res.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.PATCH)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(getPatchValidator())
                                 .prePersist(BlockingTaskWorkflow.create(prePersistTask().orElse(null)))
                                 .persist(getModificationStep(EventAction.PATCH))
                                 .postAsyncPersist(AsyncTaskWorkflow.create(postPersistAsyncTask().orElse(null)))
                                 .transformer((req, re) -> transformer().afterPatch(re.key(), re.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    /**
     * {@inheritDoc}
     */
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        return DefaultDMLWorkflow.builder()
                                 .action(EventAction.REMOVE)
                                 .metadata(context())
                                 .normalize(this::onModifyingOneResource)
                                 .validator(getDeletionValidator())
                                 .prePersist(BlockingTaskWorkflow.create(prePersistTask().orElse(null)))
                                 .persist(getDeletionStep())
                                 .postAsyncPersist(AsyncTaskWorkflow.create(postPersistAsyncTask().orElse(null)))
                                 .transformer((req, re) -> transformer().afterDelete(re.pojo(), req))
                                 .build()
                                 .execute(requestData);
    }

    @Override
    public @NonNull EntityMetadata resourceMetadata() {
        return context();
    }

    protected Observable<? extends VertxPojo> doGetMany(RequestData reqData) {
        return queryExecutor().findMany(reqData);
    }

    protected Single<? extends VertxPojo> doGetOne(RequestData reqData) {
        return queryExecutor().findOneByKey(reqData);
    }

    protected OperationValidator getCreationValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(context().onCreating(req)));
    }

    protected CreationStep getCreationStep() {
        return CreationStep.builder().queryExecutor(queryExecutor()).build();
    }

    protected OperationValidator getPatchValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onPatching(dbEntity, req)));
    }

    protected OperationValidator getUpdateValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onUpdating(dbEntity, req)));
    }

    protected ModificationStep getModificationStep(EventAction action) {
        return ModificationStep.builder().action(action).queryExecutor(queryExecutor()).build();
    }

    protected OperationValidator getDeletionValidator() {
        return OperationValidator.create((req, dbEntity) -> Single.just(validation().onDeleting(req)));
    }

    protected DeletionStep getDeletionStep() {
        return DeletionStep.builder().queryExecutor(queryExecutor()).build();
    }

}
