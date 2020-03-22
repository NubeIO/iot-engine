package com.nubeiot.edge.installer;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jooq.Catalog;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.MetadataIndex;
import com.nubeiot.core.sql.SchemaHandler;
import com.nubeiot.core.sql.decorator.EntityConstraintHolder;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.installer.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.model.DefaultCatalog;
import com.nubeiot.edge.installer.model.Keys;
import com.nubeiot.edge.installer.model.Tables;
import com.nubeiot.edge.installer.model.dto.RequestedServiceData;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationDao;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationHistoryDao;
import com.nubeiot.edge.installer.model.tables.daos.DeployTransactionDao;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplication;
import com.nubeiot.edge.installer.model.tables.interfaces.IApplicationHistory;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.model.tables.pojos.DeployTransaction;
import com.nubeiot.edge.installer.repository.InstallerRepository;
import com.nubeiot.edge.installer.service.AppDeployerDefinition;
import com.nubeiot.edge.installer.service.InstallerApiIndex;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class InstallerEntityHandler extends AbstractEntityHandler
    implements InstallerApiIndex, EntityConstraintHolder {

    public static final String SHARED_MODULE_RULE = "MODULE_RULE";
    public static final String SHARED_INSTALLER_CFG = "INSTALLER_CFG";
    public static final String SHARED_APP_DEPLOYER_CFG = "APP_DEPLOYER_CFG";
    @Getter(value = AccessLevel.PACKAGE)
    private EventAction bootstrap;

    protected InstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public @NonNull Catalog catalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final Single<EntityHandler> before() {
        InstallerConfig installerCfg = sharedData(SHARED_INSTALLER_CFG);
        return super.before().map(handler -> {
            InstallerRepository.create(handler.vertx()).setup(installerCfg.getRepoConfig(), dataDir());
            return handler;
        });
    }

    @Override
    public final @NonNull SchemaHandler schemaHandler() {
        return new InstallerSchemaHandler();
    }

    @Override
    public final @NonNull EntityConstraintHolder holder() {
        return this;
    }

    @Override
    public final @NonNull MetadataIndex metadataIndex() {
        return this;
    }

    public final ApplicationDao applicationDao() {
        return dao(ApplicationDao.class);
    }

    public final DeployTransactionDao transDao() {
        return dao(DeployTransactionDao.class);
    }

    final Single<List<Application>> getModulesWhenBootstrap() {
        return applicationDao().findManyByState(Arrays.asList(State.NONE, State.ENABLED));
    }

    final InstallerEntityHandler initDeployer() {
        final AppDeployerDefinition definition = sharedData(SHARED_APP_DEPLOYER_CFG);
        definition.register(this);
        return this;
    }

    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, IApplication tblModule, AppConfig appConfig) {
        return appConfig;
    }

    protected Application decorateModule(Application module) {
        final OffsetDateTime now = DateTimes.now();
        return module.setCreatedAt(now).setModifiedAt(now);
    }

    Single<JsonObject> addBuiltinApps(InstallerConfig config) {
        bootstrap = EventAction.INIT;
        if (config.getBuiltinApps().isEmpty()) {
            return Single.just(new JsonObject().put("status", Status.SUCCESS));
        }
        final Path dataDir = dataDir();
        return Observable.fromIterable(config.getBuiltinApps())
                         .map(serviceData -> createTblModule(dataDir, config.getRepoConfig(), serviceData))
                         .map(this::decorateModule)
                         .collect(ArrayList<Application>::new, ArrayList::add)
                         .flatMap(list -> dao(ApplicationDao.class).insert(list))
                         .map(r -> new JsonObject().put("results", "Inserted " + r + " app module record(s)"));
    }

    private Application createTblModule(Path dataDir, RepositoryConfig repoConfig, RequestedServiceData serviceData) {
        ModuleTypeRule rule = sharedData(SHARED_MODULE_RULE);
        IApplication tblModule = rule.parse(serviceData.getMetadata());
        AppConfig appConfig = transformAppConfig(repoConfig, tblModule, serviceData.getAppConfig());
        return (Application) rule.parse(dataDir, tblModule, appConfig).setState(State.NONE);
    }

    Single<JsonObject> transitionPendingModules() {
        bootstrap = EventAction.MIGRATE;
        final ApplicationDao dao = applicationDao();
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

    private Optional<DeployTransaction> getLastWipTransaction(Application module, DSLContext dsl) {
        return Optional.ofNullable(dsl.select()
                                      .from(Tables.APPLICATION)
                                      .where(DSL.field(Tables.APPLICATION.APP_ID).eq(module.getAppId()))
                                      .and(DSL.field(Tables.DEPLOY_TRANSACTION.STATUS).eq(Status.WIP))
                                      .orderBy(Tables.DEPLOY_TRANSACTION.MODIFIED_AT.desc())
                                      .limit(1)
                                      .fetchOneInto(DeployTransaction.class));
    }

    private Application checkingTransaction(Application module, DeployTransaction transaction) {
        if (transaction.getEvent() == EventAction.CREATE || transaction.getEvent() == EventAction.INIT) {
            return module.setState(State.ENABLED);
        }
        if (transaction.getEvent() == EventAction.UPDATE || transaction.getEvent() == EventAction.PATCH) {
            JsonObject prevMeta = transaction.getPrevMetadata();
            if (Objects.isNull(prevMeta)) {
                return module.setState(State.ENABLED);
            }
            return module.setState(
                new Application(prevMeta).getState() == State.DISABLED ? State.DISABLED : State.NONE);
        }
        return module.setState(State.DISABLED);
    }

    private Single<Optional<JsonObject>> findHistoryTransactionById(String transactionId) {
        return dao(ApplicationHistoryDao.class).findOneById(transactionId)
                                               .map(optional -> optional.map(IApplicationHistory::toJson));
    }

    public final Single<Optional<JsonObject>> findTransactionById(String transactionId) {
        return transDao().findOneById(transactionId)
                         .flatMap(optional -> optional.isPresent()
                                              ? Single.just(Optional.of(optional.get().toJson()))
                                              : this.findHistoryTransactionById(transactionId));
    }

    public final Single<List<DeployTransaction>> findTransactionByModuleId(String moduleId) {
        return transDao().queryExecutor()
                         .findMany(dsl -> dsl.selectFrom(Tables.DEPLOY_TRANSACTION)
                                             .where(DSL.field(Tables.DEPLOY_TRANSACTION.APP_ID).eq(moduleId))
                                             .orderBy(Tables.DEPLOY_TRANSACTION.ISSUED_AT.desc()));
    }

    public final Single<Optional<JsonObject>> findOneTransactionByModuleId(String moduleId) {
        return transDao().queryExecutor()
                         .findOne(dsl -> dsl.selectFrom(Tables.DEPLOY_TRANSACTION)
                                            .where(DSL.field(Tables.DEPLOY_TRANSACTION.APP_ID).eq(moduleId))
                                            .orderBy(Tables.DEPLOY_TRANSACTION.ISSUED_AT.desc())
                                            .limit(1))
                         .map(optional -> optional.map(DeployTransaction::toJson));
    }

    @Override
    public final @NonNull Class keyClass() {
        return Keys.class;
    }

}
