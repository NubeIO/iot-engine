package com.nubeiot.edge.core;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.AbstractEntityHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.Tables;
import com.nubeiot.edge.core.model.dto.RequestedServiceData;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.edge.core.model.tables.interfaces.ITblRemoveHistory;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;
import com.nubeiot.edge.core.repository.InstallerRepository;
import com.nubeiot.edge.core.service.AppDeployer;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class InstallerEntityHandler extends AbstractEntityHandler {

    public static final String SHARED_MODULE_RULE = "MODULE_RULE";
    public static final String SHARED_INSTALLER_CFG = "INSTALLER_CFG";
    public static final String SHARED_APP_DEPLOYER_CFG = "APP_DEPLOYER_CFG";
    @Getter(value = AccessLevel.PACKAGE)
    private EventAction bootstrap;

    protected InstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public Single<EntityHandler> before() {
        InstallerConfig installerCfg = sharedData(SHARED_INSTALLER_CFG);
        return super.before().map(handler -> {
            InstallerRepository.create(handler).setup(installerCfg.getRepoConfig(), dataDir());
            return handler;
        });
    }

    @Override
    public boolean isNew() {
        final boolean isNew = isNew(Tables.TBL_MODULE);
        bootstrap = isNew ? EventAction.INIT : EventAction.MIGRATE;
        return isNew;
    }

    @Override
    public Single<EventMessage> initData() {
        final InstallerConfig config = sharedData(SHARED_INSTALLER_CFG);
        return addBuiltinApps(config).map(results -> EventMessage.success(EventAction.INIT, results));
    }

    @Override
    public Single<EventMessage> migrate() {
        return transitionPendingModules().map(r -> EventMessage.success(EventAction.MIGRATE, r));
    }

    InstallerEntityHandler initDeployer() {
        final AppDeployer appDeployer = sharedData(SHARED_APP_DEPLOYER_CFG);
        appDeployer.register(this);
        return this;
    }

    protected abstract AppConfig transformAppConfig(RepositoryConfig repoConfig, RequestedServiceData serviceData,
                                                    ITblModule tblModule, AppConfig appConfig);

    public TblModuleDao moduleDao() {
        return dao(TblModuleDao.class);
    }

    public TblTransactionDao transDao() {
        return dao(TblTransactionDao.class);
    }

    Single<List<TblModule>> getModulesWhenBootstrap() {
        return moduleDao().findManyByState(Arrays.asList(State.NONE, State.ENABLED));
    }

    private Single<JsonObject> addBuiltinApps(InstallerConfig config) {
        if (config.getBuiltinApps().isEmpty()) {
            return Single.just(new JsonObject().put("status", Status.SUCCESS));
        }
        final Path dataDir = dataDir();
        return Observable.fromIterable(config.getBuiltinApps())
                         .map(serviceData -> createTblModule(dataDir, config.getRepoConfig(), serviceData))
                         .map(this::decorateModule)
                         .collect(ArrayList<TblModule>::new, ArrayList::add)
                         .flatMap(list -> dao(TblModuleDao.class).insert(list))
                         .map(r -> new JsonObject().put("results", "Inserted " + r + " app module record(s)"));
    }

    protected TblModule decorateModule(TblModule module) {
        final OffsetDateTime now = DateTimes.now();
        return module.setCreatedAt(now).setModifiedAt(now);
    }

    private TblModule createTblModule(Path dataDir, RepositoryConfig repoConfig, RequestedServiceData serviceData) {
        ModuleTypeRule rule = sharedData(SHARED_MODULE_RULE);
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

    private Single<Optional<JsonObject>> findHistoryTransactionById(String transactionId) {
        return dao(TblRemoveHistoryDao.class).findOneById(transactionId)
                                             .map(optional -> optional.map(ITblRemoveHistory::toJson));
    }

}
