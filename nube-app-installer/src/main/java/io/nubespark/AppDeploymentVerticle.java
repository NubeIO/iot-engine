package io.nubespark;

import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by topsykretts on 4/28/18.
 */
public class AppDeploymentVerticle extends MicroServiceVerticle {

    //receiving address
    public static String ADDRESS_INSTALLER = "io.nubespark.app.installer";

    //sending address
    public static String ADDRESS_INSTALLER_REPORT = "io.nubespark.app.installer.report";

    Logger logger = LoggerFactory.getLogger(AppDeploymentVerticle.class);
    JDBCClient jdbc;

    @Override
    public void start() {
        super.start();
        jdbc = JDBCClient.createShared(vertx, config());
        System.out.println("Config on app installer");
        System.out.println(Json.encodePrettily(config()));

        initializeDB((nothing)-> loadDeploymentsOnStartup(next-> {
            if(next.succeeded()) {
                logger.info("Nube App Installer Started Up..");
            } else {
                logger.error("Nube App Installer Startup failed..");
                next.cause().printStackTrace();
            }
        }));

        vertx.eventBus().consumer(ADDRESS_INSTALLER, this::installer);

        publishMessageSource(ADDRESS_INSTALLER, ADDRESS_INSTALLER, ar-> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Nube App Installer (Message source) published : " + ar.succeeded());
            }
        });
    }

    private void initializeDB(Handler<AsyncResult<Void>> next) {
        logger.info("Initializing the sqlite database");
        jdbc.getConnection( handler-> {
            if(handler.failed()) {
                handleFailure(handler);
             } else {
                SQLConnection connection = handler.result();
                String createTable = "CREATE TABLE IF NOT EXISTS deployed_verticles (deploymentId varchar(255), serviceName varchar(500))";
                connection.execute(createTable, createHandler-> {
                    if(createHandler.failed()) {
                        connection.close();
                        handleFailure(createHandler);
                    } else {
                        connection.close();
                        next.handle(Future.succeededFuture());
                    }
                });
            }

        });
    }

    private void loadDeploymentsOnStartup(Handler<AsyncResult<Void>> step) {
        logger.info("Loading deployed verticles on startup if any");
        jdbc.getConnection(handler -> {
            if(handler.failed()) {
                handleFailure(handler);
            } else {
                SQLConnection connection = handler.result();
                getAllDeployments(connection, allDeployments -> {
                    List<JsonObject> records = allDeployments.result();
                    for(JsonObject record: records) {
                        logger.info("Starting installed deployments..");
                        String deploymentId = record.getString("deploymentId");
                        String verticleName = record.getString("serviceName");
                        handleInstall(verticleName, next-> {
                            if(next.succeeded()) {
                                String query = "DELETE FROM deployed_verticles where deploymentId = ?";
                                JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                                connection.updateWithParams(query, params, deleteHandler-> {
                                    if(deleteHandler.failed()) {
                                        handleFailure(deleteHandler);
                                    } else {
                                        logger.info("Clearing earlier deploymentId ", deploymentId, " success.");
                                    }
                                });
                                step.handle(Future.succeededFuture());
                            } else {
                                step.handle(Future.failedFuture(next.cause()));
                            }
                        });
                    }
                });
            }
        });
    }


    private void installer(Message<Object> message) {

        String where = message.headers().get("where"); //where to deploy the verticle?
        String action = message.headers().get("action"); //action can be "install"/"uninstall"/"update"
        if (action == null) {
            action = "install";
        }
        logger.info("Host Name = ", System.getProperty("host.name"));
        if (where == null || where.equals("all") || where.equals(System.getProperty("host.name"))) {
            logger.info("Executing action ", action);
            String msg = message.body().toString();
            JsonObject info = new JsonObject(msg);
            String groupId = info.getString("groupId", "io.nubespark");
            String artifactId = info.getString("artifactId");
            String version = info.getString("version", "1.0-SNAPSHOT");

            if (artifactId != null) {
                String service = info.getString("service", artifactId);
                String verticleName = "maven:" + groupId + ":" + artifactId + ":" + version + "::" + service;
                
                final String finalAction = action;
                checkIfServiceRunning(verticleName, isRunning -> {
                    JsonObject jsonObject = isRunning.result();
                    Boolean isVerticleRunning = jsonObject.getBoolean("isRunning");
                    if(!isVerticleRunning) {
                        //service not running
                        if("install".equals(finalAction) || "update".equals(finalAction)) {
                            handleInstall(verticleName, next-> {
                                if(next.succeeded()) {
                                    Future.succeededFuture();
                                } else {
                                    Future.failedFuture(next.cause());
                                }
                            });
                        } else if ("uninstall".equals(finalAction)) {
                            logger.warn("Service is not installed ::", verticleName);
                        }
                    } else {
                        //service running
                        if("uninstall".equals(finalAction)) {
                            String deploymentId = jsonObject.getString("deploymentId");
                            handleUnInstall(deploymentId, verticleName);
                        }
                        //// TODO: 4/28/18 update logic: check version, install new, uninstall old
                    }
                });
            } else {
                logger.warn("artifactId is null. Cannot " + action);
            }
        }
    }

    private void handleUnInstall(String deploymentId, String verticleName) {
        vertx.undeploy(deploymentId, unInstallHandler-> jdbc.getConnection(handler -> {
           if(handler.failed()) {
               handleFailure(handler);
           } else {
               SQLConnection connection = handler.result();
               String query = "DELETE FROM deployed_verticles where deploymentId = ?";
               JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
               connection.updateWithParams(query, params, deleteHandler-> {
                   if(deleteHandler.failed()) {
                       JsonObject report = new JsonObject(); //reporting failure
                       report.put("serverId", "localhost-app-installer");
                       report.put("cause", deleteHandler.cause().getMessage());
                       report.put("serviceName", verticleName);
                       vertx.eventBus().publish(ADDRESS_INSTALLER_REPORT, report, new DeliveryOptions()
                               .addHeader("status", "FAILED"));

                       handleFailure(deleteHandler);
                   } else {
                       JsonObject report = new JsonObject(); //reporting deployment back to store
                       report.put("serverId", "localhost-app-installer");
                       report.put("deploymentId", deploymentId);
                       report.put("serviceName", verticleName);
                       vertx.eventBus().publish(ADDRESS_INSTALLER_REPORT, report, new DeliveryOptions()
                               .addHeader("status", "UNINSTALLED"));
                       Future.succeededFuture();

                   }
               });
           }
        }));
    }

    private void handleInstall(String verticleName, Handler<AsyncResult<Void>> next) {
        vertx.deployVerticle(verticleName,
                ar -> {
                    if (ar.succeeded()) {
                        logger.info("Successfully deployed ", verticleName);
                        String deploymentId = ar.result();
                        saveData(deploymentId, verticleName); //persisting deployment info

                        JsonObject report = new JsonObject(); //reporting deployment back to store
                        report.put("serverId", "localhost-app-installer");
                        report.put("deploymentId", deploymentId);
                        report.put("serviceName", verticleName);
                        vertx.eventBus().publish(ADDRESS_INSTALLER_REPORT, report, new DeliveryOptions()
                                .addHeader("status", "INSTALLED"));
                        next.handle(Future.succeededFuture());
                    } else {
                        logger.error("Failed to deploy verticle ", verticleName);

                        JsonObject report = new JsonObject(); //reporting failure
                        report.put("serverId", "localhost-app-installer");
                        report.put("cause", ar.cause().getMessage());
                        report.put("serviceName", verticleName);
                        vertx.eventBus().publish(ADDRESS_INSTALLER_REPORT, report, new DeliveryOptions()
                                .addHeader("status", "FAILED"));
                        handleFailure(ar);
                        next.handle(Future.failedFuture(ar.cause()));
                    }
                });
    }

    private void saveData(String deploymentID, String serviceName) {
        String insertQuery = "INSERT INTO deployed_verticles (deploymentId, serviceName) VALUES (?,?)";
        JsonArray params = new JsonArray(Arrays.asList(deploymentID, serviceName));
        jdbc.getConnection(handler -> {
            if(handler.failed()) {
                handleFailure(handler);
            }
            SQLConnection connection = handler.result();
            connection.updateWithParams(insertQuery, params, insertHandler -> {
                if(insertHandler.failed()) {
                    handleFailure(insertHandler);
                } else {
                    logger.info("Persisting ", deploymentID, " and ", serviceName, "in database");
                }
            });

        });
    }

    private void checkIfServiceRunning(String serviceName, Handler<AsyncResult<JsonObject>> next) {
        jdbc.getConnection(handler -> {
            if(handler.failed()) {
                handleFailure(handler);
            } else {
                SQLConnection connection = handler.result();
                String query = "SELECT * FROM deployed_verticles where serviceName = ?";
                JsonArray params = new JsonArray(Collections.singletonList(serviceName));
                connection.queryWithParams(query, params, selectHandler-> {
                    if(selectHandler.failed()) {
                        handleFailure(selectHandler);
                    } else {
                        if(selectHandler.result().getNumRows() > 0) {
                            logger.info("Already running Service: ", serviceName);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.put("isRunning", true);
                            jsonObject.put("deploymentId", selectHandler.result().getRows().get(0).getValue("deploymentId"));
                            logger.debug("return value = ", jsonObject);
                            next.handle(Future.succeededFuture(jsonObject));
                        } else {
                            logger.info("Not running Service: ", serviceName);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.put("isRunning", false);
                            next.handle(Future.succeededFuture(jsonObject));
                        }
                    }
                });
            }

        });
    }

    private void getAllDeployments(SQLConnection connection, Handler<AsyncResult<List<JsonObject>>> next) {
        logger.info("Getting previous deployments if any..");
        String query = "SELECT * FROM deployed_verticles";
                connection.query(query, selectHandler-> {
                   if(selectHandler.failed()) {
                       handleFailure(selectHandler);
                   } else {
                       System.out.println(Json.encodePrettily(selectHandler.result().getRows()));
                       next.handle(Future.succeededFuture(selectHandler.result().getRows()));
                   }
                });
    }

    private void handleFailure(AsyncResult handler) {
        logger.error(handler.cause().getMessage());
        Future.failedFuture(handler.cause());
    }
}
