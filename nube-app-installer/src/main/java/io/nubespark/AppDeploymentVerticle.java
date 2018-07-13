package io.nubespark;

import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
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
import io.vertx.servicediscovery.types.MessageSource;

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
        jdbc = JDBCClient.createNonShared(vertx, config());
        System.out.println(this.getClass().getCanonicalName() + " Loader = " + AppDeploymentVerticle.class.getClassLoader());
        System.out.println("Current thread loader = " + Thread.currentThread().getContextClassLoader());
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


        publishMessageSource(ADDRESS_INSTALLER, ADDRESS_INSTALLER, ar-> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Nube App Installer (Message source) published : " + ar.succeeded());
            }
        });

        MessageSource.getConsumer(discovery, new JsonObject().put("name", ADDRESS_INSTALLER), message->{
            if(message.failed()) {
                logger.error(message.cause().getMessage());
                message.cause().printStackTrace();
            } else {
                message.result().handler(this::installer);
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
                String createTable = "CREATE TABLE IF NOT EXISTS deployed_verticles (deploymentId varchar(255), serviceName varchar(500), config varchar(5000))";
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
                    for(int i=0; i<records.size(); i++) {
                        logger.info("Starting installed deployments..");
                        JsonObject record = records.get(i);
                        String deploymentId = record.getString("deploymentId");
                        String verticleName = record.getString("serviceName");
                        String configString = record.getString("config");
                        JsonObject config;
                        if(configString != null) {
                            config = new JsonObject(configString);
                        } else {
                            config = null;
                        }
                        // TODO: 5/22/18 change in swagger
                        final int finalI1 = i;
                        handleInstall(verticleName, config, next-> {
                            if(next.succeeded()) {
                                String query = "DELETE FROM deployed_verticles where deploymentId = ?";
                                JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                                connection.updateWithParams(query, params, deleteHandler-> {
                                    if(deleteHandler.failed()) {
                                        handleFailure(deleteHandler);
                                        if(finalI1 == records.size()-1) {
                                            connection.close();
                                        }
                                        step.handle(Future.failedFuture(next.cause()));
                                    } else {
                                        logger.info("Clearing earlier deploymentId ", deploymentId, " success.");
                                        if(finalI1 == records.size()-1) {
                                            connection.close();
                                        }
                                        step.handle(Future.succeededFuture());
                                    }
                                });
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
            logger.info("Executing action " + action);
            String msg = message.body().toString();
            JsonObject info = new JsonObject(msg);
            String groupId = info.getString("groupId", "io.nubespark");
            String artifactId = info.getString("artifactId");
            String version = info.getString("version", "1.0-SNAPSHOT");
            JsonObject config = info.getJsonObject("config", null);

            if (artifactId != null) {
                String service = info.getString("service", artifactId);
                String verticleName = "maven:" + groupId + ":" + artifactId + ":" + version + "::" + service;
                final String finalAction = action;
                checkIfServiceRunning(verticleName, isRunning -> {
                    logger.info("Finished checking service..");
                    JsonObject jsonObject = isRunning.result();
                    Boolean isVerticleRunning = jsonObject.getBoolean("isRunning");
                    if(!isVerticleRunning) {
                        //service not running
                        if("install".equals(finalAction) || "update".equals(finalAction)) {
                            handleInstall(verticleName, config, next-> {
                                if(next.succeeded()) {
                                    Future.succeededFuture();
//                                    System.out.println("Classpath of Nube App installer = "+ System.getProperty("java.class.path"));
//                                    URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//                                    for(URL url: urlClassLoader.getURLs()) {
//                                        System.out.println(url.getPath());
//                                    }
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
        logger.info("Handling uninstall");
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

    private void handleInstall(String verticleName, JsonObject config, Handler<AsyncResult<Void>> next) {
        logger.info("handling install");
        DeploymentOptions options = new DeploymentOptions();
        String configString = null;
        if (config != null) {
            logger.info("Loading config in deployment options:: ", Json.encodePrettily(config));
            options.setConfig(config);
            configString = config.toString();
        }
        String finalConfigString = configString;
        vertx.deployVerticle(verticleName,
                options,
                ar -> {
                    if (ar.succeeded()) {
                        logger.info("Successfully deployed ", verticleName);
                        String deploymentId = ar.result();
                        saveData(deploymentId, verticleName, finalConfigString); //persisting deployment info

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

    private void saveData(String deploymentID, String serviceName, String config) {
        String insertQuery = "INSERT INTO deployed_verticles (deploymentId, serviceName, config) VALUES (?,?,?)";
        JsonArray params = new JsonArray(Arrays.asList(deploymentID, serviceName, config));
        jdbc.getConnection(handler -> {
            if(handler.failed()) {
                handleFailure(handler);
            }
            SQLConnection connection = handler.result();
            connection.updateWithParams(insertQuery, params, insertHandler -> {
                if(insertHandler.failed()) {
                    handleFailure(insertHandler);
                    connection.close();
                } else {
                    logger.info("Persisting ", deploymentID, " and ", serviceName, "in database");
                    connection.close();
                }
            });

        });
    }

    private void checkIfServiceRunning(String serviceName, Handler<AsyncResult<JsonObject>> next) {
        logger.info("Checking if service is running: "+ serviceName);
        jdbc.getConnection(handler -> {
            if(handler.failed()) {
                logger.error("JDBC connection Failed. ", handler.cause().getMessage());
                handleFailure(handler);
            } else {
                SQLConnection connection = handler.result();
                String query = "SELECT * FROM deployed_verticles where serviceName = ?";
                JsonArray params = new JsonArray(Collections.singletonList(serviceName));
                connection.queryWithParams(query, params, selectHandler-> {
                    if(selectHandler.failed()) {
                        logger.error("SQL execution failed. ", selectHandler.cause().getMessage());
                        handleFailure(selectHandler);
                        connection.close();
                    } else {
                        if(selectHandler.result().getNumRows() > 0) {
                            logger.info("Already running Service: ", serviceName);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.put("isRunning", true);
                            jsonObject.put("deploymentId", selectHandler.result().getRows().get(0).getValue("deploymentId"));
                            logger.debug("return value = ", jsonObject);
                            connection.close();
                            next.handle(Future.succeededFuture(jsonObject));
                        } else {
                            logger.info("Not running Service: ", serviceName);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.put("isRunning", false);
                            connection.close();
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
