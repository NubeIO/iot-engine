package io.nubespark;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.utils.HttpException;
import io.nubespark.utils.Runner;
import io.nubespark.utils.StringUtils;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

/**
 * Created by topsykretts on 4/30/18.
 */
public class OsDeploymentVerticle extends RxMicroServiceVerticle {

    // Conventions for our OS
    private static final String groupId = "io.nubespark";
    private static final String artifactId = "nube-app-installer";
    private static final String service = "nube-app-installer";
    // Database Queries ========================
    private static final String CREATE_TABLE
            = "CREATE TABLE IF NOT EXISTS app_installer (deploymentId varchar(255), version varchar(255), optionsJson varchar(2000))";
    private static final String INSERT_APP_INSTALLER_QUERY
            = "INSERT INTO app_installer (deploymentId, version, optionsJson) VALUES (?, ?, ?)";
    private static final String SELECT_APP_INSTALLER_QUERY = "SELECT * FROM app_installer";
    private static final String DELETE_APP_INSTALLER_QUERY = "DELETE FROM app_installer where deploymentId = ?";
    private Logger logger = LoggerFactory.getLogger(OsDeploymentVerticle.class);
    private List<String> remotes;

    //     Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-bios/src/main/java/";
        Runner.runExample(JAVA_DIR, OsDeploymentVerticle.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        super.start();
        logger.info(this.getClass().getCanonicalName() + " Loader = " + OsDeploymentVerticle.class.getClassLoader());
        logger.info("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        logger.info("Config on nube bios");
        logger.info(Json.encodePrettily(config()));
        JsonArray def = new JsonArray().add("http://192.168.1.68:8081/repository/maven-releases/").add(
                "http://192.168.1.68:8081/repository/maven-snapshots/").add(
                "http://192.168.1.68:8081/repository/maven-central/");
        remotes = config().getJsonArray("remotes", def).getList();
        setupMavenRepos();
        this.initDB(initJDBCClient()).subscribe(resultSet -> {
            logger.info("Updated: {} rows", resultSet.getUpdated());
            logger.info("Finished OS startup");
        }, throwable -> logger.error("Error on OS startup...", throwable));
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    private JDBCClient initJDBCClient() {
        logger.info("Initializing JDBC client for local sqlite db");
        JsonObject jdbcConfig = new JsonObject();
        jdbcConfig.put("url", config().getValue("url", "jdbc:sqlite:nube-bios.db"));
        jdbcConfig.put("driver_class", config().getValue("driver_class", "org.sqlite.JDBC"));
        return JDBCClient.createNonShared(vertx, jdbcConfig);
    }

    private Single<SQLConnection> getConnection(SQLClient jdbcClient) {
        return jdbcClient.rxGetConnection().doOnError(
                throwable -> logger.error("Cannot get connection object.", throwable));
    }

    private Single<UpdateResult> initDB(JDBCClient jdbcClient) {
        return getConnection(jdbcClient).flatMap(conn -> conn.rxUpdate(CREATE_TABLE).flatMap(r -> loadOsFromMaven(conn))
                                                             .doFinally(conn::close));
    }

    private void setupMavenRepos() {
        logger.info("Setting up maven local and remote repo");
        String local = System.getProperty("user.home") + "/.m2/repository";
        logger.info("Maven local repositories: {}", local);
        logger.info("Maven remote repositories: {}", remotes);
        vertx.getDelegate().registerVerticleFactory(new MavenVerticleFactory(
                new ResolverOptions().setLocalRepository(local).setRemoteRepositories(remotes)));
    }

    private Single<UpdateResult> loadOsFromMaven(SQLConnection conn) {
        // For us OS is the NubeAppInstaller, where we can find core logic
        logger.info("Loading Nube App Installer OS ...");
        return getInstalledOs(conn).flatMap(records -> {
            boolean autoInstall = config().getBoolean("autoInstall", true);
            if (records.size() == 0 && autoInstall) {
                return handleInstall("1.0-SNAPSHOT", new JsonObject().put("remotes", remotes), conn);
            } else {
                JsonObject record = records.get(0);
                String version = record.getString("version");
                String optionsJson = record.getString("optionsJson");
                String deploymentId = record.getString("deploymentId");
                JsonObject config = new JsonObject(optionsJson);
                JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                return handleInstall(version, config, conn).flatMap(
                        result -> conn.rxUpdateWithParams(DELETE_APP_INSTALLER_QUERY, params));
            }
        });
    }

    private Single<UpdateResult> handleInstall(String version, JsonObject config, SQLConnection conn) {
        String serviceName = getServiceName(version);
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);
        return vertx.rxDeployVerticle(serviceName, deploymentOptions).flatMap(deploymentId -> {
            if (StringUtils.isNotNull(deploymentId)) {
                logger.info("Successfully deployed the Nube App Installer.");
                return saveData(deploymentId, version, config, conn); //persisting deployment info
            } else {
                logger.error("Failed to deploy the Nube App Installer.");
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "");
            }
        });
    }

    private Single<UpdateResult> saveData(String deploymentID, String version, JsonObject optionsJson,
                                          SQLConnection conn) {
        JsonArray params = new JsonArray(Arrays.asList(deploymentID, version, Json.encode(optionsJson)));
        logger.info("Persisting {} for OS version {} in database with config {}", deploymentID, version, optionsJson);
        return conn.rxUpdateWithParams(INSERT_APP_INSTALLER_QUERY, params);
    }

    private String getServiceName(String version) {
        return "maven:" + groupId + ":" + artifactId + ":" + version + "::" + service;
    }

    private Single<List<JsonObject>> getInstalledOs(SQLConnection conn) {
        return conn.rxQuery(SELECT_APP_INSTALLER_QUERY).map(ResultSet::getRows);
    }

}
