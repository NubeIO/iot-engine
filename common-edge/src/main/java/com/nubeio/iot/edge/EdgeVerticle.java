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
import com.nubeio.iot.edge.model.gen.tables.daos.TblTransactionDao;
import com.nubeio.iot.edge.model.gen.tables.pojos.TblModule;
import com.nubeio.iot.share.MicroserviceConfig;
import com.nubeio.iot.share.IMicroVerticle;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventType;
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
    protected EntityHandler entityHandler;

    @Override
    public final void start() throws Exception {
        super.start();
        this.microserviceConfig = IMicroVerticle.initConfig(vertx, config()).onStart();
        this.moduleLoader = new ModuleLoader(() -> vertx);
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

    protected abstract Single<JsonObject> initData();

    protected Single<JsonObject> startupModules() {
        return this.entityHandler.findModulesInBootstrap()
                                 .flattenAsObservable(tblModules -> tblModules)
                                 .flatMapSingle(this::updateModule)
                                 .collect(JsonArray::new, JsonArray::add)
                                 .map(results -> new JsonObject().put("results", results));
    }

    private Single<SQLClient> initDBConnection() {
        logger.info("Create Hikari datasource");
        HikariConfig config = new HikariConfig(Configs.loadPropsConfig("hikari.properties"));
        config.setJdbcUrl(String.format("jdbc:sqlite:%s.db", getDBName()));
        config.addDataSourceProperty("databaseName", getDBName());
        config.setPoolName(getDBName() + "_pool");
        this.dataSource = new HikariDataSource(config);
        this.jooqConfig = new DefaultConfiguration().set(SQLDialect.SQLITE).set(new HikariDataSource(config));
        this.entityHandler = new EntityHandler(() -> new TblModuleDao(jooqConfig, vertx),
                                               () -> new TblTransactionDao(jooqConfig, vertx),
                                               () -> new JDBCRXGenericQueryExecutor(jooqConfig, vertx));
        return Single.just(
                JDBCClient.newInstance(io.vertx.ext.jdbc.JDBCClient.create(vertx.getDelegate(), dataSource)));
    }

    private Single<List<Integer>> createDatabase(SQLClient sqlClient) {
        logger.info("Create database...");
        String fileContent = Strings.convertToString(
                this.getClass().getClassLoader().getResourceAsStream("sql/model.sql"));
        logger.debug("SQL::{}", fileContent);
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
                   .doOnSuccess(result -> logger.info("Create Database success: {}", result))
                   .doOnError(throwable -> {
                       throw new DatabaseException("Cannot create database", throwable);
                   })
                   .doFinally(conn::close);
    }

    protected Single<JsonObject> initModule(TblModule module) {
        return this.installModule(module, EventType.INIT);
    }

    protected Single<JsonObject> installModule(TblModule module) {
        return this.installModule(module, EventType.CREATE);
    }

    protected Single<JsonObject> haltModule(TblModule module) {
        return this.installModule(module, EventType.HALT);
    }

    protected Single<JsonObject> updateModule(TblModule module) {
        return this.installModule(module, EventType.UPDATE);
    }

    protected Single<JsonObject> uninstallModule(TblModule module) {
        return this.installModule(module, EventType.REMOVE);
    }

    private Single<JsonObject> installModule(TblModule module, EventType eventType) {
        logger.info("{} module with data {}", eventType, module.toJson().encode());
        return this.entityHandler.handlePreDeployment(module, eventType)
                                 .map(tranId -> new JsonObject().put("transaction", tranId)
                                                                .put("message", "Work in progress")
                                                                .put("status", Status.WIP))
                                 .doOnSuccess(r -> deployModule(module, eventType, r.getString("transaction")));
    }

    private void deployModule(TblModule module, EventType event, String transId) {
        final String serviceId = module.getServiceId();
        vertxInteractWithModule(serviceId, module.getDeployConfigJson(), event).subscribe(
                id -> entityHandler.succeedPostDeployment(serviceId, transId, event, id),
                t -> entityHandler.handleErrorPostDeployment(serviceId, transId, event, t));
    }

    private Single<String> vertxInteractWithModule(String serviceId, JsonObject deployConfig, EventType action) {
        if (EventType.CREATE == action || EventType.INIT == action) {
            logger.info("Install module {} in physical...", serviceId);
            return moduleLoader.installModule(serviceId, deployConfig);
        }
        if (EventType.UPDATE == action) {
            logger.info("Reload module {} in physical...", serviceId);
            return moduleLoader.reloadModule(serviceId, deployConfig);
        }
        if (EventType.REMOVE == action) {
            logger.info("Remove module {} in physical...", serviceId);
            return moduleLoader.removeModule(serviceId);
        }
        throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Not support action " + action);
    }

}
