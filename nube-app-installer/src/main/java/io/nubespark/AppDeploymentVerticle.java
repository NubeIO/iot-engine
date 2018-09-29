package io.nubespark;

import java.util.Arrays;
import java.util.Collections;

import io.nubespark.events.Event;
import io.nubespark.events.EventMessage;
import io.nubespark.exceptions.DatabaseException;
import io.nubespark.exceptions.EngineException;
import io.nubespark.exceptions.NubeException;
import io.nubespark.exceptions.StateException;
import io.nubespark.utils.StringUtils;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

/**
 * Created by topsykretts on 4/28/18.
 */
public class AppDeploymentVerticle extends RxMicroServiceVerticle {

    // Database Queries ========================
    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS deployed_verticles (deploymentId varchar(255), serviceName varchar(500), " +
            "config varchar(5000))";
    private static final String INSERT_VERTICLE_QUERY
            = "INSERT INTO deployed_verticles (deploymentId, serviceName, config) VALUES (?,?,?)";
    private static final String SELECT_VERTICLE_QUERY = "SELECT * FROM deployed_verticles where serviceName = ?";
    private static final String SELECT_VERTICLES_QUERY = "SELECT * FROM deployed_verticles";
    private static final String DELETE_VERTICLE_QUERY = "DELETE FROM deployed_verticles where deploymentId = ?";

    private static Logger logger = LoggerFactory.getLogger(AppDeploymentVerticle.class);
    private SQLClient sqlClient;

    @Override
    public void start() {
        super.start();
        logger.info(this.getClass().getCanonicalName() + " Loader = " + AppDeploymentVerticle.class.getClassLoader());
        logger.info("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        logger.info("Config on app installer");
        logger.info(Json.encodePrettily(config()));
        this.sqlClient = initSqlClient();
        this.initDB(this.sqlClient).subscribe(result -> {
            logger.info("Result: {}", result.encode());
            logger.info("Finished App Installer startup ...");
            logger.info("Add event bus");
            getVertx().eventBus().consumer(Event.CONTROL_MODULE.getAddress(), this::installer);
        }, throwable -> logger.error("Error to install App Installer...", throwable));
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    private JDBCClient initSqlClient() {
        logger.info("Initializing JDBC client for local sqlite db");
        JsonObject jdbcConfig = new JsonObject();
        jdbcConfig.put("url", config().getValue("url", "jdbc:sqlite:nube-app-installer.db"));
        jdbcConfig.put("driver_class", config().getValue("driver_class", "org.sqlite.JDBC"));
        return JDBCClient.createNonShared(vertx, jdbcConfig);
    }

    private Single<JsonObject> initDB(SQLClient sqlClient) {
        return getConnection(sqlClient).flatMap(conn -> conn.rxUpdate(CREATE_TABLE)
                                                            .flatMap(ignore -> loadDeploymentsOnStartup(conn))
                                                            .doFinally(conn::close));
    }

    private Single<SQLConnection> getConnection(SQLClient sqlClient) {
        return sqlClient.rxGetConnection()
                        .doOnError(throwable -> logger.error("Cannot get connection object.", throwable));
    }

    private Single<JsonObject> loadDeploymentsOnStartup(SQLConnection conn) {
        logger.info("Loading deployed verticles on startup if any.");
        return conn.rxQuery(SELECT_VERTICLES_QUERY)
                   .map(ResultSet::getRows)
                   .flattenAsObservable(records -> records)
                   .collectInto(new JsonObject(), (result, record) -> doDeployment(conn, result, record));
    }

    private void doDeployment(SQLConnection conn, JsonObject result, JsonObject record) {
        logger.info("Starting installed deployments...");
        String deploymentId = record.getString("deploymentId");
        String serviceName = record.getString("serviceName");
        String configString = record.getString("config");
        JsonObject config = StringUtils.isNull(configString) ? new JsonObject() : new JsonObject(configString);
        handleInstall(serviceName, config, conn, "install").subscribe(
                r -> result.put("install", ((JsonArray) result.getValue("install", new JsonArray())).add(r.toJson())),
                throwable -> {
                    JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                    conn.rxUpdateWithParams(DELETE_VERTICLE_QUERY, params);
                    result.put("failed", (Integer) result.getValue("failed", 0) + 1);
                });
    }

    private void installer(Message<Object> message) {
        EventMessage msg = EventMessage.from(message.body());
        // action can be "install"/"uninstall"/"update"
        logger.info("Executing action: {} with data: {}", msg.getAction(), msg.toJson().encode());
        JsonObject body = msg.getData();
        String groupId = body.getString("groupId", "io.nubespark");
        String artifactId = body.getString("artifactId");
        String version = body.getString("version", "1.0-SNAPSHOT");
        JsonObject config = body.getJsonObject("config", new JsonObject());
        if (StringUtils.isNotNull(artifactId)) {
            String service = body.getString("service", artifactId);
            String verticleName = "maven:" + groupId + ":" + artifactId + ":" + version + "::" + service;
            Single<EventMessage> result = getConnection(this.sqlClient).flatMap(
                    conn -> handleAction(msg.getAction(), config, verticleName, conn).doAfterTerminate(conn::close));
            result.subscribe(success -> message.reply(success.toJson()), throwable -> {
                logger.error("Cannot deploy new service", throwable);
                message.reply(EventMessage.error(msg.getAction(), throwable).toJson());
            });
        } else {
            logger.warn("Artifact Id is null. Cannot {}", msg.getAction());
            message.reply(EventMessage.error(msg.getAction(), NubeException.ErrorCode.INVALID_ARGUMENT,
                                             "ArtifactId is null"));
        }
    }

    private Single<EventMessage> handleAction(String action, JsonObject config, String verticleName,
                                              SQLConnection conn) {
        return checkIfServiceRunning(verticleName, conn).flatMap(
                status -> dispatchAction(conn, config, verticleName, action, status));
    }

    private Single<EventMessage> dispatchAction(SQLConnection conn, JsonObject config, String verticleName,
                                                String action, JsonObject status) {
        boolean isRunning = status.getBoolean("isRunning");
        if (isRunning) {
            if ("uninstall".equals(action)) {
                String deploymentId = status.getString("deploymentId");
                return handleUninstall(deploymentId, conn, action);
            }
            throw new StateException("Service is running, you only can uninstall!");
        }
        if ("install".equals(action) || "update".equals(action)) {
            return handleInstall(verticleName, config, conn, action);
        }
        if ("uninstall".equals(action)) {
            logger.warn("Service is not installed: ", verticleName);
            throw new StateException("Service is not installed!");
        }
        throw new StateException("You are limited to install/uninstall/update!");
    }

    private Single<EventMessage> handleUninstall(String deploymentId, SQLConnection conn, String action) {
        logger.info("Handling un-installation ...");
        JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
        return vertx.rxUndeploy(deploymentId).doOnError(throwable -> {
            throw new EngineException(throwable);
        }).mergeWith(conn.rxUpdateWithParams(DELETE_VERTICLE_QUERY, params).doOnError(throwable -> {
            throw new DatabaseException(throwable);
        }).map(UpdateResult::toJson).toCompletable()).toSingle(() -> EventMessage.success(action));
    }

    private Single<EventMessage> handleInstall(String serviceName, JsonObject config, SQLConnection conn,
                                               String action) {
        logger.info("Handling installation ...");
        logger.info("Config: {}", config.encode());
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        return vertx.rxDeployVerticle(serviceName, options)
                    .doOnError(throwable -> {
                        logger.error("what the fuck", throwable);
                        throw new EngineException(throwable);
                    })
                    .flatMap(deploymentId -> saveData(deploymentId, serviceName, config.encode(), conn).flatMap(
                            r -> constructResponse(deploymentId, serviceName)))
                    .map(data -> EventMessage.success(action, data));
    }

    private Single<JsonObject> constructResponse(String deploymentId, String serviceName) {
        return Single.just(new JsonObject().put("service_name", serviceName)
                                           .put("deployment_id", deploymentId)
                                           .put("status", "PUBLISHED"));
    }

    private Single<JsonObject> saveData(String deploymentID, String serviceName, String config, SQLConnection conn) {
        JsonArray params = new JsonArray(Arrays.asList(deploymentID, serviceName, config));
        logger.info("Persisting deployment ID: {} and service name: {}", deploymentID, serviceName);
        return conn.rxUpdateWithParams(INSERT_VERTICLE_QUERY, params).map(UpdateResult::toJson).doOnError(t -> {
            throw new DatabaseException("Cannot persist data in deployed_verticles", t);
        });
    }

    private Single<JsonObject> checkIfServiceRunning(String serviceName, SQLConnection conn) {
        logger.info("Checking if service is running: " + serviceName);
        JsonArray params = new JsonArray(Collections.singletonList(serviceName));
        return conn.rxQueryWithParams(SELECT_VERTICLE_QUERY, params)
                   .map(ResultSet::getRows)
                   .map(results -> {
                       JsonObject status = new JsonObject();
                       status.put("isRunning", results.size() > 0);
                       status.put("deploymentId", results.size() > 0 ? results.get(0).getValue("deploymentId") : null);
                       return status;
                   })
                   .doOnSuccess(status -> logger.info("Service: {} - {}", serviceName, status.encode()))
                   .doOnError(logger::error);
    }

}
