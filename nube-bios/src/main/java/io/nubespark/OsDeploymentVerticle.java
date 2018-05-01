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
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by topsykretts on 4/30/18.
 */
public class OsDeploymentVerticle extends MicroServiceVerticle {

    JDBCClient jdbc;
    Logger logger = LoggerFactory.getLogger(OsDeploymentVerticle.class);

    //conventions for our OS
    private static final String groupId = "io.nubespark";
    private static final String artifactId = "nube-app-installer";
    private static final String service = "nube-app-installer";

    private static final String ADDRESS_BIOS_REPORT = "io.nubespark.bios.report";
    private static final String ADDRESS_BIOS = "io.nubespark.bios";

    @Override
    public void start() {
        super.start();

        initializeJDBCClient(
                void1 -> initializeDB(
                    void2 -> setupMavenRepos(
                        void3 -> loadOsFromMaven(
                                void4 -> {
                                    if(void4.succeeded()) {
                                        logger.info("Finished OS startup..");
                                    } else {
                                        logger.error("Error on OS startup");
                                        void4.cause().printStackTrace();
                                    }
                                }
                        )
                )
        ));
        vertx.eventBus().consumer(ADDRESS_BIOS, this::installer);
        publishMessageSource(ADDRESS_BIOS, ADDRESS_BIOS, ar-> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Nube App Bios (Message source) published : " + ar.succeeded());
            }
        });
    }

    private void initializeJDBCClient(Handler<AsyncResult<Void>> next) {
        logger.info("Initializing JDBC client for local sqlite db");
        JsonObject jdbcConfig = new JsonObject();
        jdbcConfig.put("url", "jdbc:sqlite:nube-bios.db");
        jdbcConfig.put("driver_class", "org.sqlite.JDBC");
        jdbc = JDBCClient.createShared(vertx, jdbcConfig);
        next.handle(Future.succeededFuture());
    }

    private void setupMavenRepos(Handler<AsyncResult<Void>> next) {
        logger.info("Setting up maven local and remote repo");
        String local = System.getProperty("user.home")+"/.m2/repository";
        List<String> remotes = config().getJsonArray("remotes", new JsonArray(Arrays.asList(
                "http://192.168.1.68:8081/repository/maven-releases/",
                "http://192.168.1.68:8081/repository/maven-snapshots/",
                "http://192.168.1.68:8081/repository/maven-central/"
        ))).getList();

        vertx.registerVerticleFactory(new MavenVerticleFactory(
                new ResolverOptions()
                        .setLocalRepository(local)
                        .setRemoteRepositories(remotes))
        );
        next.handle(Future.succeededFuture());
    }

    private void initializeDB(Handler<AsyncResult<Void>> next) {
        logger.info("Initializing sqlite db with table schema");
        jdbc.getConnection( handler-> {
            if(handler.failed()) {
                handleFailure(logger, handler);
            } else {
                SQLConnection connection = handler.result();
                String createTable = "CREATE TABLE IF NOT EXISTS app_installer (deploymentId varchar(255), " +
                        "version varchar(255), optionsJson varchar(2000))";
                connection.execute(createTable, createHandler-> {
                    if(createHandler.failed()) {
                        connection.close();
                        handleFailure(logger, createHandler);
                    } else {
                        connection.close();
                        next.handle(Future.succeededFuture());
                    }
                });
            }

        });
    }

    private void loadOsFromMaven(Handler<AsyncResult<Void>> next) {
        logger.info("Loading Nube App Installer OS...");
        jdbc.getConnection(handler -> {
            if(handler.failed()) {
                handleFailure(logger, handler);
            } else {
                SQLConnection connection = handler.result();
                getInstalledOs(connection, installedOs-> {
                    List<JsonObject> records = installedOs.result();
                    boolean autoInstall = config().getBoolean("autoInstall", true);
                    if (records.size() == 0 && autoInstall) {
                        String version = "1.0-SNAPSHOT";
                        JsonObject options = new JsonObject();
                        handleInstall(version, options, success-> {
                            if(success.succeeded()) {
                                next.handle(Future.succeededFuture());
                            } else {
                                next.handle(Future.failedFuture(success.cause()));
                            }
                        });

                    } else {
                        JsonObject record = records.get(0);
                        String version = record.getString("version");
                        String optionsJson = record.getString("optionsJson");
                        String deploymentId = record.getString("deploymentId");
                        JsonObject options = new JsonObject(optionsJson);
                        handleInstall(version, options, success-> {
                            if(success.succeeded()) {
                                String query = "DELETE FROM app_installer where deploymentId = ?";
                                JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                                connection.updateWithParams(query, params, deleteHandler-> {
                                    if(deleteHandler.failed()) {
                                        handleFailure(logger, deleteHandler);
                                    } else {
                                        logger.info("Clearing earlier deploymentId ", deploymentId, " success.");
                                        next.handle(Future.succeededFuture());
                                    }
                                });
                            } else {
                                next.handle(Future.failedFuture(success.cause()));
                            }
                        });
                    }
                }
            );
            }
    });
    }

    private void handleInstall(String version, JsonObject options, Handler<AsyncResult<Void>> next) {

        String serviceName = getServiceName(version);
        DeploymentOptions deploymentOptions = new DeploymentOptions(config().mergeIn(options));
        vertx.deployVerticle(serviceName, deploymentOptions, handler-> {
            if (handler.succeeded()) {
                logger.info("Successfully deployed ", serviceName);
                String deploymentId = handler.result();
                saveData(deploymentId, version, options); //persisting deployment info
                JsonObject report = new JsonObject(); //reporting deployment back to store
                report.put("serverId", "localhost-app-installer");
                report.put("deploymentId", deploymentId);
                report.put("serviceName", serviceName);
                vertx.eventBus().publish(ADDRESS_BIOS_REPORT, report, new DeliveryOptions()
                        .addHeader("status", "INSTALLED"));
                next.handle(Future.succeededFuture());
            } else {
                logger.error("Failed to deploy verticle ", serviceName);
                JsonObject report = new JsonObject(); //reporting failure
                report.put("serverId", "localhost-app-installer");
                report.put("cause", handler.cause().getMessage());
                report.put("serviceName", serviceName);
                vertx.eventBus().publish(ADDRESS_BIOS_REPORT, report, new DeliveryOptions()
                        .addHeader("status", "FAILED"));
//                handleFailure(logger, handler);
                next.handle(Future.failedFuture(handler.cause()));
            }
        });
    }

    private void saveData(String deploymentID, String version, JsonObject optionsJson) {
        String insertQuery = "INSERT INTO app_installer (deploymentId, version, optionsJson) VALUES (?,?, ?)";
        JsonArray params = new JsonArray(Arrays.asList(deploymentID, version, Json.encode(optionsJson)));
        jdbc.getConnection(handler -> {
            if(handler.failed()) {
                handleFailure(logger, handler);
            }
            SQLConnection connection = handler.result();
            connection.updateWithParams(insertQuery, params, insertHandler -> {
                if(insertHandler.failed()) {
                    handleFailure(logger, insertHandler);
                } else {
                    logger.info("Persisting ", deploymentID, " for OS version ", version, "in database with config ", optionsJson);
                }
            });

        });
    }

    private String getServiceName(String version) {
        return "maven:" + groupId + ":" + artifactId + ":" + version + "::" + service;
    }

    private void getInstalledOs(SQLConnection connection, Handler<AsyncResult<List<JsonObject>>> next) {
        String query = "SELECT * FROM app_installer";
        connection.query(query, selectHandler-> {
            if(selectHandler.failed()) {
                handleFailure(logger, selectHandler);
            } else {
                logger.info("Installed OS: ", Json.encodePrettily(selectHandler.result().getRows()));
                next.handle(Future.succeededFuture(selectHandler.result().getRows()));
            }
        });
    }

    private void installer(Message<Object> message) {
        String where = message.headers().get("where"); //where to deploy the verticle?
        logger.info("Host Name = ", System.getProperty("host.name"));
        if (where == null || where.equals("all") || where.equals(System.getProperty("host.name"))) {
            String msg = message.body().toString();
            JsonObject info = new JsonObject(msg);
            logger.info("Message = ", Json.encodePrettily(info));
            String version = info.getString("version");
            JsonObject options = info.getJsonObject("options");
            jdbc.getConnection(handler-> {
                if(handler.failed()) {
                    handleFailure(logger, handler);
                } else {
                    SQLConnection connection = handler.result();
                    getInstalledOs(connection, installedOs-> {
                        List<JsonObject> records = installedOs.result();
                        if (records.size() == 0) {
                            handleInstall(version, options, next-> {
                                if(next.failed()) {
                                    Future.failedFuture(next.cause());
                                } else {
                                    Future.succeededFuture();
                                }
                            });
                        } else {
                            JsonObject record = records.get(0);
                            String deploymentId = record.getString("deploymentId");
                            handleInstall(version, options, next-> {
                                if(next.succeeded()) {
                                    String query = "DELETE FROM app_installer where deploymentId = ?";
                                    JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                                    connection.updateWithParams(query, params, deleteHandler-> {
                                        if(deleteHandler.failed()) {
                                            handleFailure(logger, deleteHandler);
                                        } else {
                                            logger.info("Clearing earlier deploymentId ", deploymentId, " success.");
                                            vertx.undeploy(deploymentId);
                                            logger.info("Deployments = ", vertx.deploymentIDs().toString());
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });


        }
    }

}
