package com.nubeiot.edge.core;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.Tables;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.interfaces.ITblRemoveHistory;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;
import com.nubeiot.edge.core.repository.InstallerRepository;

import lombok.NonNull;

public abstract class InstallerEntityHandler extends AbstractEntityHandler {

    private EventAction bootstrapAction;

    protected InstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public Single<EntityHandler> before() {
        InstallerConfig installerCfg = sharedData(InstallerVerticle.SHARED_INSTALLER_CFG);
        return super.before().map(handler -> {
            InstallerRepository.create(handler).setup(installerCfg.getRepoConfig(), dataDir());
            return handler;
        });
    }

    @Override
    public boolean isNew() {
        final boolean isNew = isNew(Tables.TBL_MODULE);
        bootstrapAction = isNew ? EventAction.INIT : EventAction.MIGRATE;
        return isNew;
    }

    @Override
    public Single<EventMessage> initData() {
        final InstallerConfig cfg = sharedData(InstallerVerticle.SHARED_INSTALLER_CFG);
        final EventAction action = EventAction.INIT;
        return addApps(dataDir(), cfg.getRepoConfig(), cfg.getBuiltinApps()).map(r -> EventMessage.success(action, r));
    }

    @Override
    public Single<EventMessage> migrate() {
        return transitionPendingModules().map(r -> EventMessage.success(EventAction.MIGRATE, r));
    }

    protected abstract EventModel deploymentEvent();

    protected abstract AppConfig transformAppConfig(RepositoryConfig repoConfig, RequestedServiceData serviceData,
                                                    ITblModule tblModule, AppConfig appConfig);

    public TblModuleDao moduleDao() {
        return dao(TblModuleDao.class);
    }

    public TblTransactionDao transDao() {
        return dao(TblTransactionDao.class);
    }

    protected Single<JsonObject> startAppModules() {
        return this.getModulesWhenBootstrap()
                   .flattenAsObservable(tblModules -> tblModules)
                   .flatMapSingle(module -> processDeploymentTransaction(module, bootstrapAction))
                   .collect(JsonArray::new, JsonArray::add)
                   .map(results -> new JsonObject().put("results", results));
    }

    public Single<JsonObject> processDeploymentTransaction(ITblModule module, EventAction action) {
        logger.info("INSTALLER start executing {} service {}...", action, module.getServiceId());
        return createPreDeployment(module, action).doOnSuccess(this::deployModule).map(PreDeploymentResult::toResponse);
    }

    private void deployModule(PreDeploymentResult preDeployResult) {
        EventAction action = preDeployResult.getAction();
        logger.info("INSTALLER trigger deploying for {}::::{}", preDeployResult.getServiceId(), action);
        preDeployResult.setSilent(EventAction.REMOVE == action && State.DISABLED == preDeployResult.getPrevState());
        eventClient().request(DeliveryEvent.from(deploymentEvent(), action, preDeployResult.toRequestData()));
    }

    private Single<List<TblModule>> getModulesWhenBootstrap() {
        return moduleDao().findManyByState(Arrays.asList(State.NONE, State.ENABLED));
    }

    private Single<JsonObject> addApps(Path dataDir, RepositoryConfig repoConfig,
                                       List<RequestedServiceData> builtinApps) {
        if (builtinApps.isEmpty()) {
            return Single.just(new JsonObject().put("status", Status.SUCCESS));
        }
        return Observable.fromIterable(builtinApps)
                         .map(serviceData -> createTblModule(dataDir, repoConfig, serviceData))
                         .map(this::decorateModule)
                         .collect(ArrayList<TblModule>::new, ArrayList::add)
                         .flatMap(list -> dao(TblModuleDao.class).insert(list))
                         .map(results -> new JsonObject().put("results", results));
    }

    protected TblModule decorateModule(TblModule module) {
        final OffsetDateTime now = DateTimes.now();
        return module.setCreatedAt(now).setModifiedAt(now);
    }

    private TblModule createTblModule(Path dataDir, RepositoryConfig repoConfig, RequestedServiceData serviceData) {
        ModuleTypeRule rule = sharedData(InstallerVerticle.SHARED_MODULE_RULE);
        ITblModule tblModule = rule.parse(serviceData.getMetadata());
        AppConfig appConfig = transformAppConfig(repoConfig, serviceData, tblModule, serviceData.getAppConfig());
        return (TblModule) rule.parse(dataDir, tblModule, appConfig).setState(State.NONE);
    }

    private Single<JsonObject> transitionPendingModules() {
        final TblModuleDao dao = moduleDao();
        return dao.findManyByState(Collections.singletonList(State.PENDING))
                  .flattenAsObservable(pendingModules -> pendingModules)
                  .flatMapMaybe(m -> genericQuery().executeAny(context -> getLastWipTransaction(m, context))
                                                   .filter(Optional::isPresent)
                                                   .map(Optional::get)
                                                   .map(transaction -> checkingTransaction(m, transaction)))
                  .flatMapSingle(dao::update)
                  .reduce(0, Integer::sum)
                  .map(r -> new JsonObject().put("results", r));
    }

    private Optional<TblTransaction> getLastWipTransaction(TblModule module, DSLContext dsl) {
        return Optional.ofNullable(dsl.select()
                                      .from(Tables.TBL_TRANSACTION)
                                      .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID).eq(module.getServiceId()))
                                      .and(DSL.field(Tables.TBL_TRANSACTION.STATUS).eq(Status.WIP))
                                      .orderBy(Tables.TBL_TRANSACTION.MODIFIED_AT.desc())
                                      .limit(1)
                                      .fetchOneInto(TblTransaction.class));
    }

    private TblModule checkingTransaction(TblModule module, TblTransaction transaction) {
        if (transaction.getEvent() == EventAction.CREATE || transaction.getEvent() == EventAction.INIT) {
            return module.setState(State.ENABLED);
        }
        if (transaction.getEvent() == EventAction.UPDATE || transaction.getEvent() == EventAction.PATCH) {
            JsonObject prevMeta = transaction.getPrevMetadata();
            if (Objects.isNull(prevMeta)) {
                return module.setState(State.ENABLED);
            }
            return module.setState(new TblModule(prevMeta).getState() == State.DISABLED ? State.DISABLED : State.NONE);
        }
        return module.setState(State.DISABLED);
    }

    public Single<Optional<JsonObject>> findTransactionById(String transactionId) {
        return transDao().findOneById(transactionId)
                         .flatMap(optional -> optional.isPresent()
                                              ? Single.just(Optional.of(optional.get().toJson()))
                                              : this.findHistoryTransactionById(transactionId));
    }

    public Single<List<TblTransaction>> findTransactionByModuleId(String moduleId) {
        return transDao().queryExecutor()
                         .findMany(dsl -> dsl.selectFrom(Tables.TBL_TRANSACTION)
                                             .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID).eq(moduleId))
                                             .orderBy(Tables.TBL_TRANSACTION.ISSUED_AT.desc()));
    }

    public Single<Optional<JsonObject>> findOneTransactionByModuleId(String moduleId) {
        return transDao().queryExecutor()
                         .findOne(dsl -> dsl.selectFrom(Tables.TBL_TRANSACTION)
                                            .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID).eq(moduleId))
                                            .orderBy(Tables.TBL_TRANSACTION.ISSUED_AT.desc())
                                            .limit(1))
                         .map(optional -> optional.map(TblTransaction::toJson));
    }

    private Single<PreDeploymentResult> createPreDeployment(ITblModule module, EventAction action) {
        logger.info("INSTALLER create pre-deployment for {}::::{}...", module.getServiceId(), action);
        InstallerConfig config = sharedData(InstallerVerticle.SHARED_INSTALLER_CFG);
        if (action == EventAction.REMOVE) {
            module.setState(State.UNAVAILABLE);
        }

        if (action == EventAction.MIGRATE && module.getState() == State.PENDING) {
            module.setState(State.ENABLED);
        }

        return validateModuleState(module, action).flatMap(o -> {
            if (EventAction.INIT == action || EventAction.CREATE == action) {
                module.setState(State.NONE);
                TblModule tblModule = new TblModule(module).setDeployLocation(config.getRepoConfig().getLocal());
                return markModuleInsert(tblModule).flatMap(key -> createTransaction(key.getServiceId(), action, module))
                                                  .map(transId -> createPreDeployResult(module, transId, action,
                                                                                        module.getState(),
                                                                                        State.ENABLED));
            }

            ITblModule oldOne = o.orElseThrow(() -> new NotFoundException(""));
            State targetState = Objects.isNull(module.getState()) ? oldOne.getState() : module.getState();
            if (EventAction.UPDATE == action || EventAction.PATCH == action || action == EventAction.MIGRATE) {
                return markModuleModify(module.setDeployLocation(config.getRepoConfig().getLocal()),
                                        new TblModule(oldOne),
                                        EventAction.UPDATE == action || EventAction.MIGRATE == action).flatMap(
                    key -> createTransaction(key.getServiceId(), action, oldOne).map(
                        transId -> createPreDeployResult(key, transId, action, oldOne.getState(), targetState)));
            }
            if (EventAction.REMOVE == action) {
                return markModuleDelete(new TblModule(oldOne)).flatMap(
                    key -> createTransaction(key.getServiceId(), action, oldOne).map(
                        transId -> createPreDeployResult(key, transId, action, oldOne.getState(), targetState)));
            }
            throw new UnsupportedOperationException("Unsupported event " + action);
        });
    }

    private PreDeploymentResult createPreDeployResult(ITblModule module, String transactionId, EventAction event,
                                                      State prevState, State targetState) {
        return PreDeploymentResult.builder()
                                  .transactionId(transactionId)
                                  .action(event == EventAction.MIGRATE ? EventAction.UPDATE : event)
                                  .prevState(prevState)
                                  .targetState(targetState)
                                  .serviceId(module.getServiceId())
                                  .serviceFQN(module.getServiceType()
                                                    .generateFQN(module.getServiceId(), module.getVersion(),
                                                                 module.getServiceName()))
                                  .deployId(module.getDeployId())
                                  .appConfig(module.getAppConfig())
                                  .systemConfig(module.getSystemConfig())
                                  .dataDir(dataDir().toString())
                                  .build();
    }

    private Single<Optional<ITblModule>> validateModuleState(ITblModule module, EventAction action) {
        logger.info("INSTALLER validates service state {}::::{} ...", module.getServiceId(), action);
        return moduleDao().findOneById(module.getServiceId())
                          .map(o -> validateModuleState(o.orElse(null), action, module.getState()));
    }

    private Single<String> createTransaction(String moduleId, EventAction action, ITblModule module) {
        logger.info("INSTALLER create transaction for {}::::{}...", moduleId, action);
        if (logger.isDebugEnabled()) {
            logger.debug("Previous module state: {}", module.toJson());
        }
        final OffsetDateTime now = DateTimes.now();
        final String transactionId = UUID.randomUUID().toString();
        JsonObject metadata = module.toJson();
        // TODO: replace with POJO constant later
        metadata.remove("system_config");
        metadata.remove("app_config");
        final TblTransaction transaction = new TblTransaction().setTransactionId(transactionId)
                                                               .setModuleId(moduleId)
                                                               .setStatus(Status.WIP)
                                                               .setEvent(action)
                                                               .setIssuedAt(now)
                                                               .setModifiedAt(now)
                                                               .setRetry(0)
                                                               .setPrevMetadata(metadata)
                                                               .setPrevSystemConfig(module.getSystemConfig())
                                                               .setPrevAppConfig(module.getAppConfig());
        return transDao().insert(transaction).map(i -> transactionId);
    }

    private Single<ITblModule> markModuleInsert(ITblModule module) {
        logger.debug("INSTALLER mark service {} to create...", module.getServiceId());
        OffsetDateTime now = DateTimes.now();
        return moduleDao().insert((TblModule) module.setCreatedAt(now).setModifiedAt(now).setState(State.PENDING))
                          .map(i -> module);
    }

    private Single<ITblModule> markModuleModify(ITblModule module, ITblModule oldOne, boolean isUpdated) {
        logger.debug("INSTALLER mark service {} to modify...", module.getServiceId());
        ITblModule into = this.updateModule(oldOne, module, isUpdated);
        return moduleDao().update((TblModule) into.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                          .map(ignore -> oldOne);
    }

    private ITblModule updateModule(@NonNull ITblModule old, @NonNull ITblModule newOne, boolean isUpdated) {
        verifyUpdateModule(newOne, isUpdated);
        old.setVersion(Strings.isBlank(newOne.getVersion()) ? old.getVersion() : newOne.getVersion());
        old.setPublishedBy(Strings.isBlank(newOne.getPublishedBy()) ? old.getPublishedBy() : newOne.getPublishedBy());
        old.setState(Objects.isNull(newOne.getState()) ? old.getState() : newOne.getState());
        old.setSystemConfig(
            IConfig.merge(old.getSystemConfig(), newOne.getSystemConfig(), isUpdated, NubeConfig.class).toJson());
        old.setAppConfig(IConfig.merge(old.getAppConfig(), newOne.getAppConfig(), isUpdated, AppConfig.class).toJson());
        return old;
    }

    private void verifyUpdateModule(ITblModule newOne, boolean isUpdated) {
        if (Strings.isBlank(newOne.getVersion()) && isUpdated) {
            throw new IllegalArgumentException("Version is mandatory");
        }
        if (Objects.isNull(newOne.getState()) && isUpdated) {
            throw new IllegalArgumentException("State is mandatory");
        }
        if (Objects.isNull(newOne.getAppConfig()) && isUpdated) {
            throw new IllegalArgumentException("App config is mandatory");
        }
    }

    private Single<ITblModule> markModuleDelete(ITblModule module) {
        logger.debug("INSTALLER mark service {} to delete...", module.getServiceId());
        return moduleDao().update((TblModule) module.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                          .map(ignore -> module);
    }

    private Optional<ITblModule> validateModuleState(ITblModule findModule, EventAction action, State targetState) {
        logger.info("INSTALLER StateMachine validate action {} with {}...", action, targetState);
        StateMachine.instance().validate(findModule, action, "service");
        if (Objects.nonNull(findModule)) {
            logger.info("Module in database is found, validating conflict...");
            final State target = action == EventAction.INIT
                                 ? State.ENABLED
                                 : Optional.ofNullable(targetState).orElse(findModule.getState());
            StateMachine.instance()
                        .validateConflict(findModule.getState(), action, "service " + findModule.getServiceId(),
                                          target);
            return Optional.of(findModule);
        }
        return Optional.empty();
    }

    private Single<Optional<JsonObject>> findHistoryTransactionById(String transactionId) {
        return dao(TblRemoveHistoryDao.class).findOneById(transactionId)
                                             .map(optional -> optional.map(ITblRemoveHistory::toJson));
    }

}
