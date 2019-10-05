package com.nubeiot.edge.core;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential;
import com.nubeiot.auth.Credential.HiddenCredential;
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
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.model.Tables;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.interfaces.ITblRemoveHistory;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;
import com.nubeiot.edge.core.model.tables.records.TblTransactionRecord;
import com.nubeiot.edge.core.repository.InstallerRepository;

import lombok.NonNull;

public abstract class InstallerEntityHandler extends AbstractEntityHandler {

    protected static final String SHARED_INSTALLER_CFG = "INSTALLER_CFG";

    protected InstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public boolean isNew() {
        return isNew(Tables.TBL_MODULE);
    }

    @Override
    public Single<EventMessage> migrate() {
        return this.startupModules().map(r -> EventMessage.success(EventAction.MIGRATE, r));
    }

    @Override
    public Single<EntityHandler> before() {
        InstallerConfig installerCfg = sharedData(SHARED_INSTALLER_CFG);
        return super.before().map(handler -> {
            InstallerRepository.create(handler).setup(installerCfg.getRepoConfig(), dataDir());
            return handler;
        });
    }

    protected abstract EventModel deploymentEvent();

    public TblModuleDao getModuleDao() {
        return dao(TblModuleDao.class);
    }

    public TblTransactionDao getTransDao() {
        return dao(TblTransactionDao.class);
    }

    protected Single<JsonObject> startupModules() {
        return this.getModulesWhenBootstrap()
                   .flattenAsObservable(tblModules -> tblModules)
                   .flatMapSingle(module -> this.processDeploymentTransaction(module, EventAction.MIGRATE))
                   .collect(JsonArray::new, JsonArray::add)
                   .map(results -> new JsonObject().put("results", results));
    }

    public Single<JsonObject> processDeploymentTransaction(ITblModule module, EventAction action) {
        logger.info("{} module with data {}", action, module.toJson().encode());
        return handlePreDeployment(module, action).doAfterSuccess(this::deployModule).map(result -> {
            JsonObject appConfig = this.getSecureAppConfig(result.getServiceId(), result.getAppConfig().toJson());
            PreDeploymentResult preDeploymentResult = PreDeploymentResult.builder()
                                                                         .transactionId(result.getTransactionId())
                                                                         .action(result.getAction())
                                                                         .prevState(result.getPrevState())
                                                                         .targetState(result.getTargetState())
                                                                         .serviceId(result.getServiceId())
                                                                         .serviceFQN(result.getServiceFQN())
                                                                         .deployId(result.getDeployId())
                                                                         .appConfig(appConfig)
                                                                         .systemConfig(
                                                                             result.getSystemConfig().toJson())
                                                                         .dataDir(dataDir().toString())
                                                                         .build();
            return preDeploymentResult.toJson().put("message", "Work in progress").put("status", Status.WIP);
        });
    }

    private void deployModule(PreDeploymentResult preDeployResult) {
        String transactionId = preDeployResult.getTransactionId();
        EventAction action = preDeployResult.getAction();
        logger.info("Execute transaction: {}", transactionId);
        preDeployResult.setSilent(EventAction.REMOVE == action && State.DISABLED == preDeployResult.getPrevState());
        eventClient().request(DeliveryEvent.from(deploymentEvent(), action, preDeployResult.toRequestData()));
    }

    private Single<List<TblModule>> getModulesWhenBootstrap() {
        Single<List<TblModule>> enabledModules = getModuleDao().findManyByState(
            Collections.singletonList(State.ENABLED));
        Single<List<TblModule>> pendingModules = this.getPendingModules();
        return Single.zip(enabledModules, pendingModules, (flattenEnabledModules, flattenPendingModules) -> {
            if (Objects.nonNull(flattenEnabledModules)) {
                flattenEnabledModules.addAll(flattenPendingModules);
                return flattenEnabledModules;
            }
            return flattenPendingModules;
        });
    }

    private Single<List<TblModule>> getPendingModules() {
        return getModuleDao().findManyByState(Collections.singletonList(State.PENDING))
                             .flattenAsObservable(pendingModules -> pendingModules)
                             .flatMapSingle(m -> genericQuery().executeAny(context -> getTransactions(m, context)))
                             .filter(Optional::isPresent)
                             .map(Optional::get)
                             .collect(ArrayList::new, (modules, m) -> modules.add((TblModule) m));
    }

    private Optional<?> getTransactions(TblModule module, DSLContext dslContext) {
        List<TblTransactionRecord> records = dslContext.select()
                                                       .from(Tables.TBL_TRANSACTION)
                                                       .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID)
                                                                 .eq(module.getServiceId()))
                                                       .and(DSL.field(Tables.TBL_TRANSACTION.STATUS).eq(Status.WIP))
                                                       .orderBy(Tables.TBL_TRANSACTION.MODIFIED_AT.desc())
                                                       .limit(1)
                                                       .fetchInto(TblTransactionRecord.class);
        if (records.isEmpty()) {
            module.setState(State.DISABLED);
            getModuleDao().update(module).subscribe();
            return Optional.empty();
        }
        if (checkingTransaction(records.get(0))) {
            return Optional.of(module);
        }
        module.setState(State.DISABLED);
        getModuleDao().update(module).subscribe();
        return Optional.empty();
    }

    private boolean checkingTransaction(TblTransactionRecord transaction) {
        if (transaction.getEvent() == EventAction.CREATE || transaction.getEvent() == EventAction.INIT) {
            return true;
        }
        if (transaction.getEvent() == EventAction.UPDATE || transaction.getEvent() == EventAction.PATCH) {
            JsonObject prevMetadata = transaction.getPrevMetadata();
            if (Objects.isNull(prevMetadata)) {
                return true;
            }
            return new TblModule(prevMetadata).getState() != State.DISABLED;
        }
        return false;
    }

    protected Single<Boolean> isFreshInstall() {
        return genericQuery().executeAny(context -> context.fetchCount(Tables.TBL_MODULE)).map(count -> count == 0);
    }

    public Single<Optional<TblModule>> findModuleById(String serviceId) {
        return getModuleDao().findOneById(serviceId);
    }

    public Single<Optional<JsonObject>> findTransactionById(String transactionId) {
        return getTransDao().findOneById(transactionId)
                            .flatMap(optional -> optional.isPresent()
                                                 ? Single.just(Optional.of(optional.get().toJson()))
                                                 : this.findHistoryTransactionById(transactionId));
    }

    public Single<List<TblTransaction>> findTransactionByModuleId(String moduleId) {
        return getTransDao().queryExecutor()
                            .findMany(dslContext -> dslContext.selectFrom(Tables.TBL_TRANSACTION)
                                                              .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID)
                                                                        .eq(moduleId))
                                                              .orderBy(Tables.TBL_TRANSACTION.ISSUED_AT.desc()));
    }

    public Single<Optional<JsonObject>> findOneTransactionByModuleId(String moduleId) {
        return getTransDao().queryExecutor()
                            .findOne(dsl -> dsl.selectFrom(Tables.TBL_TRANSACTION)
                                               .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID).eq(moduleId))
                                               .orderBy(Tables.TBL_TRANSACTION.ISSUED_AT.desc())
                                               .limit(1))
                            .map(optional -> optional.map(TblTransaction::toJson));
    }

    private Single<PreDeploymentResult> handlePreDeployment(ITblModule module, EventAction event) {
        logger.info("Handle entities before do deployment...");
        InstallerConfig config = sharedData(SHARED_INSTALLER_CFG);
        if (event == EventAction.REMOVE) {
            module.setState(State.UNAVAILABLE);
        }

        if (event == EventAction.MIGRATE && module.getState() == State.PENDING) {
            module.setState(State.ENABLED);
        }

        return validateModuleState(module, event).flatMap(o -> {
            if (EventAction.INIT == event || EventAction.CREATE == event) {
                module.setState(State.NONE);
                TblModule tblModule = new TblModule(module).setDeployLocation(config.getRepoConfig().getLocal());
                return markModuleInsert(tblModule).flatMap(key -> createTransaction(key.getServiceId(), event, module))
                                                  .map(transId -> createPreDeployResult(module, transId, event,
                                                                                        module.getState(),
                                                                                        State.ENABLED));
            }

            ITblModule oldOne = o.orElseThrow(() -> new NotFoundException(""));

            State targetState = Objects.isNull(module.getState()) ? oldOne.getState() : module.getState();
            if (EventAction.UPDATE == event || EventAction.PATCH == event || event == EventAction.MIGRATE) {
                return markModuleModify(module.setDeployLocation(config.getRepoConfig().getLocal()),
                                        new TblModule(oldOne),
                                        EventAction.UPDATE == event || EventAction.MIGRATE == event).flatMap(
                    key -> createTransaction(key.getServiceId(), event, oldOne).map(
                        transId -> createPreDeployResult(key, transId, event, oldOne.getState(), targetState)));
            }
            if (EventAction.REMOVE == event) {
                return markModuleDelete(new TblModule(oldOne)).flatMap(
                    key -> createTransaction(key.getServiceId(), event, oldOne).map(
                        transId -> createPreDeployResult(key, transId, event, oldOne.getState(), targetState)));
            }
            throw new UnsupportedOperationException("Unsupported event " + event);
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

    private Single<Optional<ITblModule>> validateModuleState(ITblModule tblModule, EventAction eventAction) {
        logger.info("Validate {}::::{} ...", tblModule.getServiceId(), eventAction);
        return getModuleDao().findOneById(tblModule.getServiceId())
                             .map(o -> validateModuleState(o.orElse(null), eventAction, tblModule.getState()));
    }

    private Single<String> createTransaction(String moduleId, EventAction action, ITblModule module) {
        if (logger.isDebugEnabled()) {
            logger.debug("Create new transaction for {}::::{}...", moduleId, action);
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
        return getTransDao().insert(transaction).map(i -> transactionId);
    }

    private Single<ITblModule> markModuleInsert(ITblModule module) {
        logger.debug("Mark service {} to create...", module.getServiceId());
        OffsetDateTime now = DateTimes.now();
        return getModuleDao().insert((TblModule) module.setCreatedAt(now).setModifiedAt(now).setState(State.PENDING))
                             .map(i -> module);
    }

    private Single<ITblModule> markModuleModify(ITblModule module, ITblModule oldOne, boolean isUpdated) {
        logger.debug("Mark service {} to modify...", module.getServiceId());
        ITblModule into = this.updateModule(oldOne, module, isUpdated);
        return getModuleDao().update((TblModule) into.setState(State.PENDING).setModifiedAt(DateTimes.now()))
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
            throw new NubeException("Version is required!");
        }
        if (Objects.isNull(newOne.getState()) && isUpdated) {
            throw new NubeException("State is required!");
        }
        if (Objects.isNull(newOne.getAppConfig()) && isUpdated) {
            throw new NubeException("App config is required!");
        }
    }

    private Single<ITblModule> markModuleDelete(ITblModule module) {
        logger.debug("Mark service {} to delete...", module.getServiceId());
        return getModuleDao().update((TblModule) module.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                             .map(ignore -> module);
    }

    private Optional<ITblModule> validateModuleState(ITblModule findModule, EventAction eventAction,
                                                     State targetState) {
        logger.info("StateMachine is validating...");
        StateMachine.instance().validate(findModule, eventAction, "service");
        if (Objects.nonNull(findModule)) {
            logger.info("Module in database is found, validate conflict ");
            StateMachine.instance()
                        .validateConflict(findModule.getState(), eventAction, "service " + findModule.getServiceId(),
                                          targetState == null ? findModule.getState() : targetState);
            return Optional.of(findModule);
        }
        return Optional.empty();
    }

    private Single<Optional<JsonObject>> findHistoryTransactionById(String transactionId) {
        return dao(TblRemoveHistoryDao.class).findOneById(transactionId)
                                             .map(optional -> optional.map(ITblRemoveHistory::toJson));
    }

    public JsonObject getSecureAppConfig(String serviceId, JsonObject appConfigJson) {
        if ("com.nubeiot.edge.module:installer".equals(serviceId)) {
            logger.debug("Removing nexus password from result");
            AppConfig appConfig = IConfig.from(appConfigJson, AppConfig.class);
            Object installerObject = appConfig.get(InstallerConfig.NAME);
            if (Objects.isNull(installerObject)) {
                logger.debug("Installer config is not available");
                return appConfigJson;
            }
            InstallerConfig installerConfig = IConfig.from(installerObject, InstallerConfig.class);
            installerConfig.getRepoConfig()
                           .getRemoteConfig()
                           .getUrls()
                           .values()
                           .forEach(remoteUrl -> remoteUrl.forEach(url -> {
                               Credential credential = url.getCredential();
                               if (Objects.isNull(credential)) {
                                   return;
                               }
                               url.setCredential(new HiddenCredential(credential));
                           }));
            appConfig.put(InstallerConfig.NAME, installerConfig);
            logger.debug("Installer config {}", installerConfig.toJson().toString());
            return appConfig.toJson();
        }
        return appConfigJson;
    }

}
