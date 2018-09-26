package io.nubespark;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.utils.CustomMessage;
import io.nubespark.utils.CustomMessageCodec;
import io.nubespark.utils.CustomMessageResponseHelper;
import io.nubespark.utils.HttpException;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

import java.util.Arrays;
import java.util.Collections;


/**
 * Created by topsykretts on 4/28/18.
 */
public class AppDeploymentVerticle extends RxMicroServiceVerticle {
    private static String ADDRESS_APP_INSTALLER = "io.nubespark.app.installer"; // receiving address

    // Database Queries ========================
    private static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS deployed_verticles (deploymentId varchar(255), serviceName varchar(500), config varchar(5000))";
    private static String INSERT_VERTICLE_QUERY = "INSERT INTO deployed_verticles (deploymentId, serviceName, config) VALUES (?,?,?)";
    private static String SELECT_VERTICLE_QUERY = "SELECT * FROM deployed_verticles where serviceName = ?";
    private static String SELECT_VERTICLES_QUERY = "SELECT * FROM deployed_verticles";
    private static String DELETE_VERTICLE_QUERY = "DELETE FROM deployed_verticles where deploymentId = ?";

    private Logger logger = LoggerFactory.getLogger(AppDeploymentVerticle.class);
    private JDBCClient jdbcClient;

    @Override
    public void start() {
        super.start();
        jdbcClient = JDBCClient.createNonShared(vertx, config());
        logger.info(this.getClass().getCanonicalName() + " Loader = " + AppDeploymentVerticle.class.getClassLoader());
        logger.info("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        logger.info("Config on app installer");
        logger.info(Json.encodePrettily(config()));
        getConnection()
            .flatMap(conn -> Single.create(source -> conn.execute(CREATE_TABLE, handler -> {
                if (handler.failed()) {
                    source.onError(new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "Unable to create table"));
                } else {
                    source.onSuccess(true);
                }
            })))
            .flatMap(nothing -> loadDeploymentsOnStartup())
            .doOnSuccess(ignored -> logger.info("Successfully installed the verticles"))
            .doOnError(ignored -> logger.info("Failed to installed the verticles"))
            .map(ignored -> {
                EventBus eventBus = getVertx().eventBus();
                // Register codec for custom message
                eventBus.registerDefaultCodec(CustomMessage.class, new CustomMessageCodec());
                return eventBus.consumer(ADDRESS_APP_INSTALLER, this::installer);
            })
            .subscribe(
                ignored -> logger.info("Successfully started Nube AppDeploymentVerticle"),
                throwable -> logger.error("Failed due to: " + throwable.getCause().getMessage()));
    }

    private Single<SQLConnection> getConnection() {
        return jdbcClient.rxGetConnection()
            .flatMap(conn -> Single.just(conn)
                .doOnError(throwable -> logger.error("Cannot get connection object."))
                .doFinally(conn::close));
    }

    private Single<String> loadDeploymentsOnStartup() {
        logger.info("Loading deployed verticles on startup if any.");
        return getConnection()
            .flatMap(conn -> conn.rxQuery(SELECT_VERTICLES_QUERY))
            .map(ResultSet::getRows)
            .flatMap(records -> Observable.fromIterable(records).flatMapSingle(record -> {
                logger.info("Starting installed deployments...");
                String deploymentId = record.getString("deploymentId");
                String serviceName = record.getString("serviceName");
                String configString = record.getString("config");
                JsonObject config;
                if (configString != null) {
                    config = new JsonObject(configString);
                } else {
                    config = null;
                }

                return handleInstall(serviceName, config)
                    .flatMap(ignored -> getConnection())
                    .flatMap(conn -> {
                        JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                        return conn.rxUpdateWithParams(DELETE_VERTICLE_QUERY, params);
                    });
            }).toList())
            .map(ignored -> "");
    }


    private void installer(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        String action = customMessage.getHeader().getString("action"); // action can be "install"/"uninstall"/"update"
        JsonObject body = (JsonObject) customMessage.getBody();

        if (action == null) {
            action = "install";
        }

        String actionFinal = action;

        logger.info("Executing action " + action);
        String groupId = body.getString("groupId", "io.nubespark");
        String artifactId = body.getString("artifactId");
        String version = body.getString("version", "1.0-SNAPSHOT");
        JsonObject config = body.getJsonObject("config", new JsonObject());

        if (artifactId != null) {
            String service = body.getString("service", artifactId);
            String verticleName = "maven:" + groupId + ":" + artifactId + ":" + version + "::" + service;
            final String finalAction = action;

            checkIfServiceRunning(verticleName)
                .flatMap(status -> {
                    Boolean isRunning = status.getBoolean("isRunning");
                    if (isRunning) {
                        if ("uninstall".equals(finalAction)) {
                            String deploymentId = status.getString("deploymentId");
                            return handleUnInstall(deploymentId);
                        }
                        throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Service is running, you only can uninstall!");
                    } else {
                        if ("install".equals(finalAction) || "update".equals(finalAction)) {
                            return handleInstall(verticleName, config);
                        } else if ("uninstall".equals(finalAction)) {
                            logger.warn("Service is not installed: ", verticleName);
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Service is not installed!");
                        }
                        throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "You are limited to install/uninstall/update!");
                    }
                })
                .subscribe(
                    ignored -> message.reply(new CustomMessage<>(null,
                        new JsonObject().put("message", "Successful " + actionFinal), HttpResponseStatus.CREATED.code())),
                    throwable -> CustomMessageResponseHelper.handleHttpException(message, throwable));
        } else {
            logger.warn("ArtifactId is null. Cannot " + action);
            message.reply(new CustomMessage<>(null,
                new JsonObject().put("message", "ArtifactId is null. Cannot " + action), HttpResponseStatus.BAD_REQUEST.code()));
        }
    }

    private Single<Boolean> handleUnInstall(String deploymentId) {
        logger.info("Handling un-installation ...");
        vertx.rxUndeploy(deploymentId).subscribe();
        return getConnection()
            .flatMap(conn -> {
                JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                return conn.rxUpdateWithParams(DELETE_VERTICLE_QUERY, params);
            })
            .map(ignored -> true);
    }

    private Single<Boolean> handleInstall(String serviceName, JsonObject config) {
        logger.info("Handling installation ... \n");
        logger.info("Config: " + config);
        DeploymentOptions options = new DeploymentOptions();
        String configString = null;
        if (config != null) {
            logger.info("Loading config in deployment options: ", Json.encodePrettily(config));
            options.setConfig(config);
            configString = config.toString();
        }
        String finalConfigString = configString;

        return Single.create(source -> vertx.deployVerticle(serviceName, options, ar -> {
            if (ar.failed()) {
                logger.warn("Doesn't match the installation details.");
                source.onError(new HttpException(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), "Doesn't match the installation details."));
            } else {
                saveData(ar.result(), serviceName, finalConfigString)
                    .map(aBoolean -> {
                        source.onSuccess(true);// persisting deployment info
                        return true;
                    }).subscribe();
            }
        }));
    }

    private Single<Boolean> saveData(String deploymentID, String serviceName, String config) {
        JsonArray params = new JsonArray(Arrays.asList(deploymentID, serviceName, config));
        return getConnection()
            .flatMap(conn -> conn.rxUpdateWithParams(INSERT_VERTICLE_QUERY, params)
                .map(ignore -> {
                    logger.info("Persisting ", deploymentID, " and ", serviceName, "in database");
                    return true;
                }));
    }


    private Single<JsonObject> checkIfServiceRunning(String serviceName) {
        logger.info("Checking if service is running: " + serviceName);
        return getConnection()
            .flatMap(conn -> {
                JsonArray params = new JsonArray(Collections.singletonList(serviceName));
                return conn.rxQueryWithParams(SELECT_VERTICLE_QUERY, params);
            })
            .map(ResultSet::getRows)
            .map(results -> {
                JsonObject status = new JsonObject();
                if (results.size() > 0) {
                    status.put("isRunning", true);
                    status.put("deploymentId", results.get(0).getValue("deploymentId"));
                } else {
                    status.put("isRunning", false);
                }
                return status;
            });
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
