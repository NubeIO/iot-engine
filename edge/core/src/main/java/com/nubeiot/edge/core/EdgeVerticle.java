package com.nubeiot.edge.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jooq.Configuration;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.event.IEventHandler;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.sql.ISqlProvider;
import com.nubeiot.core.sql.SQLWrapper;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.model.gen.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.gen.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.core.model.gen.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.gen.tables.pojos.TblModule;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.reactivex.Single;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
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
    private JsonObject appConfig;

    @Override
    public final void start() throws Exception {
        this.appConfig = Configs.getApplicationCfg(config());
        this.moduleLoader = new ModuleLoader(vertx);
        this.moduleRule = this.registerModuleRule();
        registerEventBus();
        this.sqlWrapper = ISqlProvider.initConfig(this.vertx, config(), this::initData);
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

    protected abstract ModuleTypeRule registerModuleRule();

    protected abstract Single<JsonObject> initData();
    
    protected abstract List<String> getSupportGroups(ModuleType moduleType);
    
    protected Predicate<String> validateGroup(ModuleType moduleType) {
        return artifact -> {
            return this.getSupportGroups(moduleType).stream().anyMatch(item -> artifact.contains(item));
        };
    }

    protected Single<JsonObject> startupModules() {
        return this.entityHandler.getModulesWhenBootstrap()
                                 .flattenAsObservable(tblModules -> tblModules)
                                 .flatMapSingle(module -> this.processDeploymentTransaction(module, EventType.UPDATE))
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put("results", results));
    }

    protected void handleEvent(Message<Object> message, IEventHandler eventHandler) {
        EventMessage msg = EventMessage.from(message.body());
        logger.info("Executing action: {} with data: {}", msg.getAction(), msg.toJson().encode());
        try {
            eventHandler.handle(msg.getAction(), msg.getData().mapTo(RequestData.class))
                        .subscribe(data -> message.reply(EventMessage.success(msg.getAction(), data).toJson()),
                                   throwable -> {
                                       logger.error("Failed when handle event", throwable);
                                       message.reply(EventMessage.error(msg.getAction(), throwable).toJson());
                                   });
        } catch (NubeException ex) {
            logger.error("Failed when handle event", ex);
            message.reply(EventMessage.error(msg.getAction(), ex).toJson());
        }
    }

    protected Single<JsonObject> processDeploymentTransaction(TblModule module, EventType eventType) {
        logger.info("{} module with data {}", eventType, module.toJson().encode());
        return this.entityHandler.handlePreDeployment(module, eventType)
                                 .doAfterSuccess(this::deployModule)
                                 .map(result -> result.toJson()
                                                      .put("message", "Work in progress")
                                                      .put("status", Status.WIP));
    }

    private void deployModule(PreDeploymentResult preDeployResult) {
        final String transactionId = preDeployResult.getTransactionId();
        final String serviceId = preDeployResult.getServiceId();
        final EventType event = preDeployResult.getEvent();
        logger.info("Execute transaction: {}", transactionId);
        preDeployResult.setSilent(EventType.REMOVE == event && State.DISABLED == preDeployResult.getPrevState());
        final RequestData data = RequestData.builder().body(preDeployResult.toJson()).build();
        moduleLoader.handle(event, data)
                    .subscribe(r -> entityHandler.succeedPostDeployment(serviceId, transactionId, event,
                                                                        r.getString("deploy_id")),
                               t -> entityHandler.handleErrorPostDeployment(serviceId, transactionId, event, t));
    }
    
}
