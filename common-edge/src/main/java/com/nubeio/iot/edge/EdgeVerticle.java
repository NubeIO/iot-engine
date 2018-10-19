package com.nubeio.iot.edge;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import com.nubeio.iot.edge.loader.ModuleLoader;
import com.nubeio.iot.edge.model.gen.tables.daos.TblModuleDao;
import com.nubeio.iot.edge.model.gen.tables.daos.TblRemoveHistoryDao;
import com.nubeio.iot.edge.model.gen.tables.daos.TblTransactionDao;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblModule;
import com.nubeio.iot.share.IMicroVerticle;
import com.nubeio.iot.share.MicroserviceConfig;
import com.nubeio.iot.share.enums.State;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventMessage;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.event.IEventHandler;
import com.nubeio.iot.share.event.RequestData;
import com.nubeio.iot.share.exceptions.DatabaseException;
import com.nubeio.iot.share.exceptions.ErrorMessage;
import com.nubeio.iot.share.exceptions.NubeException;
import com.nubeio.iot.share.utils.Configs;
import com.nubeio.iot.share.utils.Strings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.jklingsporn.vertx.jooq.rx.jdbc.JDBCRXGenericQueryExecutor;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EdgeVerticle extends AbstractVerticle implements IMicroVerticle {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    private MicroserviceConfig microserviceConfig;
    private ModuleLoader moduleLoader;
    private DataSource dataSource;
    private Configuration jooqConfig;
    @Getter
    protected EntityHandler entityHandler;
    @Getter
    private JsonObject appConfig;

    @Override
    public final void start() throws Exception {
        super.start();
        this.appConfig = Configs.getApplicationCfg(config());
        this.microserviceConfig = IMicroVerticle.initConfig(vertx, config()).onStart();
        this.moduleLoader = new ModuleLoader(() -> vertx);
        registerEventHandler();
        initDBConnection().flatMap(client -> this.createDatabase(client).flatMap(ignores -> initData()))
                          .subscribe(logger::info, throwable -> {
                              logger.error("Failed to startup application", throwable);
                              throw new IllegalStateException(ErrorMessage.parse(throwable).toJson().encode());
                          });
    }

    @Override
    public final void stop() {
        try {
            this.dataSource.unwrap(HikariDataSource.class).close();
        } catch (SQLException e) {
            logger.debug("Unable to close datasource", e);
        }
    }

    @Override
    public void stop(Future<Void> future) {
        this.microserviceConfig.onStop(future);
    }

    protected abstract String getDBName();

    protected abstract void registerEventHandler();

    protected abstract Single<JsonObject> initData();

    String genJdbcUrl() {
        return String.format("jdbc:sqlite:%s.db", getDBName());
    }

    private Single<SQLClient> initDBConnection() {
        logger.info("Create Hikari datasource");
        HikariConfig config = new HikariConfig(Configs.loadPropsConfig("hikari.properties"));
        config.setJdbcUrl(genJdbcUrl());
        config.addDataSourceProperty("databaseName", getDBName());
        config.setPoolName(getDBName() + "_pool");
        this.dataSource = new HikariDataSource(config);
        this.jooqConfig = new DefaultConfiguration().set(SQLDialect.SQLITE).set(new HikariDataSource(config));
        this.entityHandler = new EntityHandler(() -> new TblModuleDao(jooqConfig, vertx),
                                               () -> new TblTransactionDao(jooqConfig, vertx),
                                               () -> new TblRemoveHistoryDao(jooqConfig, vertx),
                                               () -> new JDBCRXGenericQueryExecutor(jooqConfig, vertx));
        return Single.just(
                JDBCClient.newInstance(io.vertx.ext.jdbc.JDBCClient.create(vertx.getDelegate(), dataSource)));
    }

    private Single<List<Integer>> createDatabase(SQLClient sqlClient) {
        logger.info("Create database...");
        String fileContent = Strings.convertToString(
                this.getClass().getClassLoader().getResourceAsStream("sql/model.sql"));
        logger.trace("SQL::{}", fileContent);
        return sqlClient.rxGetConnection().doOnError(throwable -> {
            throw new DatabaseException("Cannot open database connection", throwable);
        }).flatMap(conn -> executeCreateDDL(conn, fileContent));
    }

    private Single<List<Integer>> executeCreateDDL(SQLConnection conn, String fileContent) {
        List<String> sqlStatements = Strings.isBlank(fileContent)
                                     ? new ArrayList<>()
                                     : Arrays.stream(fileContent.split(";"))
                                             .filter(Strings::isNotBlank)
                                             .collect(Collectors.toList());
        return conn.rxBatch(sqlStatements)
                   .doAfterSuccess(result -> logger.info("Create Database success: {}", result))
                   .doOnError(throwable -> {
                       throw new DatabaseException("Cannot create database", throwable);
                   })
                   .doFinally(conn::close);
    }

    protected Single<JsonObject> startupModules() {
        return this.entityHandler.getModulesWhenBootstrap()
                                 .flattenAsObservable(tblModules -> tblModules)
                                 .flatMapSingle(module -> this.handleModule(module, EventType.UPDATE))
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

    public Single<JsonObject> handleModule(TblModule module, EventType eventType) {
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
        boolean isSilent = EventType.REMOVE == event && State.DISABLED == preDeployResult.getPrevState();
        final RequestData data = RequestData.builder().body(preDeployResult.toJson().put("silent", isSilent)).build();
        moduleLoader.handle(event, data)
                    .subscribe(r -> entityHandler.succeedPostDeployment(serviceId, transactionId, event,
                                                                        r.getString("deploy_id")),
                               t -> entityHandler.handleErrorPostDeployment(serviceId, transactionId, event, t));
    }

}
