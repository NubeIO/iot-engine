package com.nubeiot.edge.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.core.InstallerConfig.RemoteUrl;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig.RemoteRepositoryConfig;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.Tables;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.interfaces.ITblRemoveHistory;
import com.nubeiot.edge.core.model.tables.interfaces.ITblTransaction;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblRemoveHistory;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;
import com.nubeiot.edge.core.model.tables.records.TblTransactionRecord;

import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class EdgeEntityHandler extends EntityHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TblModuleDao moduleDao;
    private final TblTransactionDao transDao;
    private final TblRemoveHistoryDao historyDao;

    protected EdgeEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        moduleDao = new TblModuleDao(jooqConfig, getVertx());
        transDao = new TblTransactionDao(jooqConfig, getVertx());
        historyDao = new TblRemoveHistoryDao(jooqConfig, getVertx());
    }

    @Override
    public boolean isNew() {
        return isNew(com.nubeiot.edge.core.model.tables.TblModule.TBL_MODULE);
    }

    @Override
    public Single<EventMessage> migrate() {
        return this.startupModules().map(r -> EventMessage.success(EventAction.MIGRATE, r));
    }

    protected abstract EventModel deploymentEvent();

    protected void setupServiceRepository(RepositoryConfig repositoryCfg) {
        logger.info("Setting up service local and remote repository");
        RemoteRepositoryConfig remoteConfig = repositoryCfg.getRemoteConfig();
        logger.info("URLs: " + remoteConfig.getUrls());
        remoteConfig.getUrls()
                    .entrySet()
                    .stream()
                    .parallel()
                    .forEach(entry -> handleVerticleFactory(repositoryCfg.getLocal(), entry));
    }

    private void handleVerticleFactory(String local, Entry<ModuleType, List<RemoteUrl>> entry) {
        final ModuleType type = entry.getKey();
        if (ModuleType.JAVA == type) {
            List<RemoteUrl> remoteUrls = entry.getValue();
            String javaLocal = FileUtils.createFolder(local, type.name().toLowerCase(Locale.ENGLISH));
            logger.info("{} local repositories: {}", type, javaLocal);
            logger.info("{} remote repositories: {}", type, remoteUrls);
            ResolverOptions resolver = new ResolverOptions().setRemoteRepositories(
                remoteUrls.stream().map(RemoteUrl::computeUrl).collect(Collectors.toList()))
                                                            .setLocalRepository(javaLocal);
            vertx.registerVerticleFactory(new MavenVerticleFactory(resolver));
        }
    }

    protected Single<JsonObject> startupModules() {
        return this.getModulesWhenBootstrap()
                   .flattenAsObservable(tblModules -> tblModules)
                   .flatMapSingle(module -> this.processDeploymentTransaction(module, EventAction.MIGRATE))
                   .collect(JsonArray::new, JsonArray::add)
                   .map(results -> new JsonObject().put("results", results));
    }

    protected Single<JsonObject> processDeploymentTransaction(ITblModule module, EventAction action) {
        logger.info("{} module with data {}", action, module.toJson().encode());
        return this.handlePreDeployment(module, action).doAfterSuccess(this::deployModule).map(result -> {
            PreDeploymentResult preDeploymentResult = PreDeploymentResult.builder()
                                                                         .transactionId(result.getTransactionId())
                                                                         .action(result.getAction())
                                                                         .prevState(result.getPrevState())
                                                                         .targetState(result.getTargetState())
                                                                         .serviceId(result.getServiceId())
                                                                         .serviceFQN(result.getServiceFQN())
                                                                         .deployId(result.getDeployId())
                                                                         .appConfig(result.getAppConfig().toJson())
                                                                         .systemConfig(
                                                                             result.getSystemConfig().toJson())
                                                                         .secretConfig(
                                                                             result.getSecretConfig().toJson())
                                                                         .dataDir((String) this.getSharedDataFunc()
                                                                                               .apply(
                                                                                                   SharedDataDelegate.SHARED_DATADIR))
                                                                         .build();
            JsonObject preDeploymentResultJson = preDeploymentResult.toJson().copy();
            preDeploymentResultJson.remove("secret_config");
            return preDeploymentResultJson.put("message", "Work in progress").put("status", Status.WIP);
        });
    }

    private void deployModule(PreDeploymentResult preDeployResult) {
        String transactionId = preDeployResult.getTransactionId();
        String serviceId = preDeployResult.getServiceId();
        EventAction action = preDeployResult.getAction();
        logger.info("Execute transaction: {}", transactionId);
        preDeployResult.setSilent(EventAction.REMOVE == action && State.DISABLED == preDeployResult.getPrevState());
        EventMessage request = EventMessage.success(action, preDeployResult.toRequestData());
        EventController controller = (EventController) sharedDataFunc.apply(SharedDataDelegate.SHARED_EVENTBUS);
        ReplyEventHandler reply = ReplyEventHandler.builder()
                                                   .action(action)
                                                   .system("VERTX_DEPLOY")
                                                   .success(r -> succeedPostDeployment(serviceId, transactionId, action,
                                                                                       r.getData()
                                                                                        .getString("deploy_id"),
                                                                                       preDeployResult.getTargetState()))
                                                   .error(e -> errorPostDeployment(serviceId, transactionId, action, e))
                                                   .build();
        controller.request(deploymentEvent().getAddress(), deploymentEvent().getPattern(), request, reply);
    }

    public Single<List<TblModule>> getModulesWhenBootstrap() {
        Single<List<TblModule>> enabledModules = moduleDao.findManyByState(Collections.singletonList(State.ENABLED));
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
        return moduleDao.findManyByState(Collections.singletonList(State.PENDING))
                        .flattenAsObservable(pendingModules -> pendingModules)
                        .flatMapSingle(module -> queryExecutor.executeAny(context -> getTransactions(module, context)))
                        .collect(ArrayList::new,
                                 (modules, optional) -> optional.ifPresent(o -> modules.add((TblModule) o)));
    }

    private Optional<?> getTransactions(TblModule module, DSLContext dslContext) {
        List<TblTransactionRecord> tblTransactionRecords = dslContext.select()
                                                                     .from(Tables.TBL_TRANSACTION)
                                                                     .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID)
                                                                               .eq(module.getServiceId()))
                                                                     .and(DSL.field(Tables.TBL_TRANSACTION.STATUS)
                                                                             .eq(Status.WIP))
                                                                     .orderBy(Tables.TBL_TRANSACTION.MODIFIED_AT.desc())
                                                                     .limit(1)
                                                                     .fetchInto(TblTransactionRecord.class);
        if (tblTransactionRecords.isEmpty()) {
            module.setState(State.DISABLED);
            moduleDao.update(module).subscribe();
            return Optional.empty();
        }
        if (checkingTransaction(tblTransactionRecords.get(0))) {
            return Optional.of(module);
        }
        module.setState(State.DISABLED);
        moduleDao.update(module).subscribe();
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

    public Single<Boolean> isFreshInstall() {
        return queryExecutor.executeAny(context -> context.fetchCount(Tables.TBL_MODULE)).map(count -> count == 0);
    }

    public Single<Optional<TblModule>> findModuleById(String serviceId) {
        return moduleDao.findOneById(serviceId);
    }

    public Single<Optional<JsonObject>> findTransactionById(String transactionId) {
        return transDao.findOneById(transactionId)
                       .flatMap(optional -> optional.isPresent()
                                            ? Single.just(Optional.of(optional.get().toJson()))
                                            : this.findHistoryTransactionById(transactionId));
    }

    public Single<List<TblTransaction>> findTransactionByModuleId(String moduleId) {
        return transDao.queryExecutor()
                       .findMany(dslContext -> dslContext.selectFrom(Tables.TBL_TRANSACTION)
                                                         .where(
                                                             DSL.field(Tables.TBL_TRANSACTION.MODULE_ID).eq(moduleId))
                                                         .orderBy(Tables.TBL_TRANSACTION.ISSUED_AT.desc()));
    }

    public Single<Optional<JsonObject>> findOneTransactionByModuleId(String moduleId) {
        return transDao.queryExecutor()
                       .findOne(dslContext -> dslContext.selectFrom(Tables.TBL_TRANSACTION)
                                                        .where(DSL.field(Tables.TBL_TRANSACTION.MODULE_ID).eq(moduleId))
                                                        .orderBy(Tables.TBL_TRANSACTION.ISSUED_AT.desc())
                                                        .limit(1))
                       .map(optional -> optional.map(TblTransaction::toJson));
    }

    private Single<PreDeploymentResult> handlePreDeployment(ITblModule module, EventAction event) {
        logger.info("Handle entities before do deployment...");
        InstallerConfig config = IConfig.from(sharedDataFunc.apply(EdgeVerticle.SHARED_INSTALLER_CFG),
                                              InstallerConfig.class);
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
                return markModuleInsert(tblModule).flatMap(
                    key -> createTransaction(key.getServiceId(), event, module).map(
                        transId -> createPreDeployResult(module, transId, event, module.getState(), State.ENABLED)));
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
                                  .secretConfig(module.getSecretConfig())
                                  .dataDir((String) this.getSharedDataFunc().apply(SharedDataDelegate.SHARED_DATADIR))
                                  .build();
    }

    private Single<Optional<ITblModule>> validateModuleState(ITblModule tblModule, EventAction eventAction) {
        logger.info("Validate {}::::{} ...", tblModule.getServiceId(), eventAction);
        return moduleDao.findOneById(tblModule.getServiceId())
                        .map(o -> validateModuleState(o.orElse(null), eventAction, tblModule.getState()));
    }

    //TODO: register EventBus to send message somewhere
    private void succeedPostDeployment(String serviceId, String transId, EventAction eventAction, String deployId,
                                       State targetState) {
        logger.info("Handle entities after success deployment...");
        final Status status = Status.SUCCESS;
        final State state = StateMachine.instance().transition(eventAction, status, targetState);
        if (State.UNAVAILABLE == state) {
            logger.info("Remove module id {} and its transactions", serviceId);
            transDao.findOneById(transId)
                    .flatMap(o -> this.createRemovedServiceRecord(o.orElse(
                        new TblTransaction().setTransactionId(transId).setModuleId(serviceId).setEvent(eventAction))
                                                                   .setStatus(status)))
                    .map(history -> transDao)
                    .flatMap(transDao -> transDao.deleteByCondition(Tables.TBL_TRANSACTION.MODULE_ID.eq(serviceId))
                                                 .flatMap(ignore -> moduleDao.deleteById(serviceId)))
                    .subscribe();
        } else {
            Map<TableField, String> values = Collections.singletonMap(Tables.TBL_MODULE.DEPLOY_ID, deployId);
            queryExecutor.executeAny(c -> updateTransStatus(c, transId, status, null))
                         .flatMap(r1 -> queryExecutor.executeAny(c -> updateModuleState(c, serviceId, state, values))
                                                     .map(r2 -> r1 + r2))
                         .subscribe();
        }
    }

    //TODO: register EventBus to send message somewhere
    private void errorPostDeployment(String serviceId, String transId, EventAction action, ErrorMessage error) {
        logger.error("Handle entities after error deployment...");
        queryExecutor.executeAny(c -> updateTransStatus(c, transId, Status.FAILED,
                                                        Collections.singletonMap(Tables.TBL_TRANSACTION.LAST_ERROR,
                                                                                 error.toJson())))
                     .flatMap(r1 -> queryExecutor.executeAny(c -> updateModuleState(c, serviceId, State.DISABLED, null))
                                                 .map(r2 -> r1 + r2))
                     .subscribe();
    }

    private Single<String> createTransaction(String moduleId, EventAction action, ITblModule module) {
        if (logger.isDebugEnabled()) {
            logger.debug("Create new transaction for {}::::{}...", moduleId, action);
            logger.debug("Previous module state: {}", module.toJson());
        }
        final LocalDateTime now = DateTimes.nowUTC();
        final String transactionId = UUID.randomUUID().toString();
        JsonObject metadata = module.toJson();
        JsonData.removeKeys(metadata, "system_config", "app_config");
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
        return transDao.insert(transaction).map(i -> transactionId);
    }

    private Single<ITblModule> markModuleInsert(ITblModule module) {
        logger.debug("Mark service {} to create...", module.getServiceId());
        LocalDateTime now = DateTimes.nowUTC();
        return moduleDao.insert((TblModule) module.setCreatedAt(now).setModifiedAt(now).setState(State.PENDING))
                        .map(i -> module);
    }

    private Single<ITblModule> markModuleModify(ITblModule module, ITblModule oldOne, boolean isUpdated) {
        logger.debug("Mark service {} to modify...", module.getServiceId());
        ITblModule into = this.updateModule(oldOne, module, isUpdated);
        return moduleDao.update((TblModule) into.setState(State.PENDING).setModifiedAt(DateTimes.nowUTC()))
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
        old.setSecretConfig(
            IConfig.merge(old.getSecretConfig(), newOne.getSecretConfig(), isUpdated, SecretConfig.class).toJson());
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
        return moduleDao.update((TblModule) module.setState(State.PENDING).setModifiedAt(DateTimes.nowUTC()))
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

    private int updateModuleState(DSLContext context, String serviceId, State state, Map<?, ?> values) {
        return context.update(Tables.TBL_MODULE)
                      .set(Tables.TBL_MODULE.STATE, state)
                      .set(Tables.TBL_MODULE.MODIFIED_AT, DateTimes.nowUTC())
                      .set(Objects.isNull(values) ? new HashMap<>() : values)
                      .where(Tables.TBL_MODULE.SERVICE_ID.eq(serviceId))
                      .execute();
    }

    private int updateTransStatus(DSLContext context, String transId, Status status, Map<?, ?> values) {
        return context.update(Tables.TBL_TRANSACTION)
                      .set(Tables.TBL_TRANSACTION.STATUS, status)
                      .set(Tables.TBL_TRANSACTION.MODIFIED_AT, DateTimes.nowUTC())
                      .set(Objects.isNull(values) ? new HashMap<>() : values)
                      .where(Tables.TBL_TRANSACTION.TRANSACTION_ID.eq(transId))
                      .execute();
    }

    private Single<ITblRemoveHistory> createRemovedServiceRecord(ITblTransaction transaction) {
        logger.info("Create History record...");
        ITblRemoveHistory history = this.convertToHistory(transaction);
        return historyDao.insert((TblRemoveHistory) history).map(i -> history);
    }

    private ITblRemoveHistory convertToHistory(ITblTransaction transaction) {
        ITblRemoveHistory history = new TblRemoveHistory().fromJson(transaction.toJson());
        if (Objects.isNull(history.getIssuedAt())) {
            history.setIssuedAt(DateTimes.nowUTC());
        }
        if (Objects.isNull(history.getModifiedAt())) {
            history.setIssuedAt(DateTimes.nowUTC());
        }
        if (Objects.isNull(history.getRetry())) {
            history.setRetry(0);
        }
        return history;
    }

    private Single<Optional<JsonObject>> findHistoryTransactionById(String transactionId) {
        return historyDao.findOneById(transactionId).map(optional -> optional.map(ITblRemoveHistory::toJson));
    }

}
