package com.nubeiot.edge.core;

import java.util.function.Supplier;

import org.jooq.Configuration;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.ISqlProvider;
import com.nubeiot.core.sql.SQLWrapper;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EdgeVerticle extends AbstractVerticle implements ISqlProvider {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    private ModuleTypeRule moduleRule;
    private ModuleLoader moduleLoader;
    private SQLWrapper sqlWrapper;
    @Getter
    protected EntityHandler entityHandler;
    @Getter
    private NubeConfig nubeConfig;

    @Override
    public final void start() throws Exception {
        this.nubeConfig = IConfig.from(config(), NubeConfig.class);
        this.moduleLoader = new ModuleLoader(vertx);
        this.moduleRule = this.getModuleRuleProvider().get();
        registerEventBus();
        this.sqlWrapper = ISqlProvider.create(this.vertx, nubeConfig, this::initData);
        this.sqlWrapper.start();
        Configuration jooqConfig = this.sqlWrapper.getJooqConfig();
        this.entityHandler = new EntityHandler(() -> new TblModuleDao(jooqConfig, vertx),
                                               () -> new TblTransactionDao(jooqConfig, vertx),
                                               () -> new TblRemoveHistoryDao(jooqConfig, vertx),
                                               () -> new JDBCRXGenericQueryExecutor(jooqConfig, vertx));
        super.start();
    }

    @Override
    public final void stop() {
        this.sqlWrapper.stop();
    }

    protected abstract void registerEventBus();

    protected abstract Supplier<ModuleTypeRule> getModuleRuleProvider();

    protected abstract Single<JsonObject> initData();

    protected Single<JsonObject> startupModules() {
        return this.entityHandler.getModulesWhenBootstrap()
                                 .flattenAsObservable(tblModules -> tblModules)
                                 .flatMapSingle(module -> this.processDeploymentTransaction(module, EventAction.UPDATE))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put("results", results));
    }

    protected Single<JsonObject> processDeploymentTransaction(ITblModule module, EventAction eventAction) {
        logger.info("{} module with data {}", eventAction, module.toJson().encode());
        return this.entityHandler.handlePreDeployment(module, eventAction)
                                 .doAfterSuccess(this::deployModule)
                                 .map(result -> result.toJson()
                                                      .put("message", "Work in progress")
                                                      .put("status", Status.WIP));
    }

    private void deployModule(PreDeploymentResult preDeployResult) {
        final String transactionId = preDeployResult.getTransactionId();
        final String serviceId = preDeployResult.getServiceId();
        final EventAction event = preDeployResult.getAction();
        logger.info("Execute transaction: {}", transactionId);
        preDeployResult.setSilent(EventAction.REMOVE == event && State.DISABLED == preDeployResult.getPrevState());
        moduleLoader.handleEvent(event, preDeployResult.toRequestData())
                    .subscribe(r -> entityHandler.succeedPostDeployment(serviceId, transactionId, event,
                                                                        r.getString("deploy_id")),
                               t -> entityHandler.handleErrorPostDeployment(serviceId, transactionId, event, t));
    }

}
