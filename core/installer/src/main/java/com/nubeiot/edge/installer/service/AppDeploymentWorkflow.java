package com.nubeiot.edge.installer.service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.installer.InstallerConfig;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.model.dto.PreDeploymentResult;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.model.tables.pojos.DeployTransaction;

import lombok.NonNull;

public final class AppDeploymentWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentWorkflow.class);

    private final InstallerEntityHandler entityHandler;
    private final AppDeployerDefinition definition;

    public AppDeploymentWorkflow(@NonNull InstallerEntityHandler entityHandler) {
        this.entityHandler = entityHandler;
        this.definition = entityHandler.sharedData(InstallerEntityHandler.SHARED_APP_DEPLOYER_CFG);
    }

    public Single<JsonObject> process(@NonNull IApplication application, @NonNull EventAction action) {
        return process(Collections.singleton(application), action).firstOrError();
    }

    public Observable<JsonObject> process(@NonNull Collection<? extends IApplication> applications,
                                          @NonNull EventAction action) {
        return Observable.fromIterable(applications).flatMapSingle(module -> processDeployment(module, action));
    }

    private Single<JsonObject> processDeployment(IApplication application, EventAction action) {
        LOGGER.info("INSTALLER handle for {}::::{}", action, application.getAppId());
        return createPreDeployment(application, action).doOnSuccess(this::deployModule)
                                                       .map(PreDeploymentResult::toResponse);
    }

    private Single<PreDeploymentResult> createPreDeployment(IApplication req, EventAction action) {
        LOGGER.info("INSTALLER create pre-deployment for {}::::{}", action, req.getAppId());
        InstallerConfig config = entityHandler.sharedData(InstallerEntityHandler.SHARED_INSTALLER_CFG);
        if (EventAction.CREATE == action || InstallerAction.isInternal(action) && State.PENDING == req.getState()) {
            req.setState(State.ENABLED);
        }
        if (EventAction.REMOVE == action) {
            req.setState(State.UNAVAILABLE);
        }
        return validateModuleState(req, action).filter(Optional::isPresent)
                                               .map(Optional::get)
                                               .defaultIfEmpty(req)
                                               .map(m -> m.setDeployLocation(config.getRepoConfig().getLocal()))
                                               .flatMapSingle(m -> persistPreDeployResult(req, action, m));
    }

    private Single<PreDeploymentResult> persistPreDeployResult(@NonNull IApplication request,
                                                               @NonNull EventAction action,
                                                               @NonNull IApplication dbEntity) {
        final Application cloneDb = new Application(dbEntity);
        final State prevState = EventAction.CREATE == action ? State.NONE : dbEntity.getState();
        final State toState = Optional.ofNullable(request.getState()).orElse(dbEntity.getState());
        Maybe<IApplication> into = Maybe.empty();
        if (InstallerAction.isInstall(action)) {
            into = Maybe.fromSingle(markModuleInsert(cloneDb));
        }
        if (InstallerAction.isUpdate(action) && !InstallerAction.isPatch(action)) {
            into = Maybe.fromSingle(markModuleModify(request, cloneDb, true));
        }
        if (InstallerAction.isPatch(action)) {
            into = Maybe.fromSingle(markModuleModify(request, cloneDb, false));
        }
        if (InstallerAction.isUninstall(action)) {
            into = Maybe.fromSingle(markModuleDelete(cloneDb));
        }
        return into.switchIfEmpty(Single.error(new UnsupportedOperationException("Unsupported event " + action)))
                   .flatMap(module -> createTransaction(action, module).map(
                       transId -> createPreDeployResult(module, transId, action, prevState, toState)));
    }

    private void deployModule(PreDeploymentResult preDeployResult) {
        EventAction action = preDeployResult.getAction();
        LOGGER.info("INSTALLER trigger deploying for {}::::{}", action, preDeployResult.getServiceId());
        preDeployResult.setSilent(EventAction.REMOVE == action && State.DISABLED == preDeployResult.getPrevState());
        entityHandler.eventClient()
                     .fire(DeliveryEvent.from(definition.getExecuterEvent(), action, preDeployResult.toRequestData()));
    }

    private PreDeploymentResult createPreDeployResult(IApplication app, String transactionId, EventAction action,
                                                      State prevState, State targetState) {
        return PreDeploymentResult.builder()
                                  .transactionId(transactionId)
                                  .action(action)
                                  .prevState(prevState)
                                  .targetState(targetState)
                                  .serviceId(app.getAppId())
                                  .serviceFQN(app.getServiceType()
                                                 .generateFQN(app.getAppId(), app.getVersion(), app.getServiceName()))
                                  .deployId(app.getDeployId())
                                  .appConfig(app.getAppConfig())
                                  .systemConfig(app.getSystemConfig())
                                  .dataDir(entityHandler.dataDir().toString())
                                  .build();
    }

    private Single<Optional<IApplication>> validateModuleState(IApplication module, EventAction action) {
        LOGGER.info("INSTALLER validate service state {}::::{}", action, module.getAppId());
        return entityHandler.applicationDao()
                            .findOneById(module.getAppId())
                            .map(o -> validateModuleState(o.orElse(null), action, module.getState()));
    }

    private Single<String> createTransaction(EventAction action, IApplication module) {
        LOGGER.info("INSTALLER create transaction for {}::::{}", action, module.getAppId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("INSTALLER previous module state: {}", module.toJson());
        }
        final OffsetDateTime now = DateTimes.now();
        final String transactionId = UUID.randomUUID().toString();
        final JsonObject metadata = module.toJson();
        // TODO replace with POJO constant later
        metadata.remove("system_config");
        metadata.remove("app_config");
        final DeployTransaction transaction = new DeployTransaction().setTransactionId(transactionId)
                                                                     .setAppId(module.getAppId())
                                                                     .setStatus(Status.WIP)
                                                                     .setEvent(action)
                                                                     .setIssuedAt(now)
                                                                     .setModifiedAt(now)
                                                                     .setRetry(0)
                                                                     .setPrevMetadata(metadata)
                                                                     .setPrevSystemConfig(module.getSystemConfig())
                                                                     .setPrevAppConfig(module.getAppConfig());
        return entityHandler.transDao().insert(transaction).map(i -> transactionId);
    }

    private Single<IApplication> markModuleInsert(IApplication app) {
        LOGGER.debug("INSTALLER mark service {} to create...", app.getAppId());
        OffsetDateTime now = DateTimes.now();
        return entityHandler.applicationDao()
                            .insert((Application) app.setCreatedAt(now).setModifiedAt(now).setState(State.PENDING))
                            .map(i -> app);
    }

    private Single<IApplication> markModuleModify(IApplication module, IApplication oldOne, boolean isUpdated) {
        LOGGER.debug("INSTALLER mark service {} to modify...", module.getAppId());
        IApplication into = updateModule(oldOne, module, isUpdated);
        return entityHandler.applicationDao()
                            .update((Application) into.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                            .map(ignore -> oldOne);
    }

    private Single<IApplication> markModuleDelete(IApplication module) {
        LOGGER.debug("INSTALLER mark service {} to delete...", module.getAppId());
        return entityHandler.applicationDao()
                            .update((Application) module.setState(State.PENDING).setModifiedAt(DateTimes.now()))
                            .map(ignore -> module);
    }

    private IApplication updateModule(@NonNull IApplication old, @NonNull IApplication newOne, boolean isUpdated) {
        if (Strings.isBlank(newOne.getVersion()) && isUpdated) {
            throw new IllegalArgumentException("Service version is mandatory");
        }
        if (Objects.isNull(newOne.getState()) && isUpdated) {
            throw new IllegalArgumentException("Service state is mandatory");
        }
        old.setVersion(Strings.fallback(newOne.getVersion(), old.getVersion()));
        old.setPublishedBy(Strings.fallback(newOne.getPublishedBy(), old.getPublishedBy()));
        old.setState(Objects.isNull(newOne.getState()) ? old.getState() : newOne.getState());
        old.setSystemConfig(
            IConfig.merge(old.getSystemConfig(), newOne.getSystemConfig(), isUpdated, NubeConfig.class).toJson());
        old.setAppConfig(IConfig.merge(old.getAppConfig(), newOne.getAppConfig(), isUpdated, AppConfig.class).toJson());
        return old;
    }

    private Optional<IApplication> validateModuleState(IApplication findModule, EventAction action, State targetState) {
        StateMachine.instance().validate(findModule, action, "service");
        if (Objects.nonNull(findModule)) {
            final State target = action == EventAction.INIT
                                 ? State.ENABLED
                                 : Optional.ofNullable(targetState).orElse(findModule.getState());
            StateMachine.instance()
                        .validateConflict(findModule.getState(), action, "service " + findModule.getAppId(), target);
            return Optional.of(findModule);
        }
        return Optional.empty();
    }

}
