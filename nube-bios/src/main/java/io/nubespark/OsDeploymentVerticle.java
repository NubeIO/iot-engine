package io.nubespark;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.utils.HttpException;
import io.nubespark.utils.Runner;
import io.nubespark.utils.StringUtils;
import io.nubespark.vertx.common.RxMicroServiceVerticle;
import io.reactivex.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.maven.MavenVerticleFactory;
import io.vertx.maven.ResolverOptions;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by topsykretts on 4/30/18.
 */
public class OsDeploymentVerticle extends RxMicroServiceVerticle {

    private Logger logger = LoggerFactory.getLogger(OsDeploymentVerticle.class);

    // Conventions for our OS
    private static final String groupId = "io.nubespark";
    private static final String artifactId = "nube-app-installer";
    private static final String service = "nube-app-installer";

    // Database Queries ========================
    private static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS app_installer (deploymentId varchar(255), version varchar(255), optionsJson varchar(2000))";
    private static String INSERT_APP_INSTALLER_QUERY = "INSERT INTO app_installer (deploymentId, version, optionsJson) VALUES (?, ?, ?)";
    private static String SELECT_APP_INSTALLER_QUERY = "SELECT * FROM app_installer";
    private static String DELETE_APP_INSTALLER_QUERY = "DELETE FROM app_installer where deploymentId = ?";

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String JAVA_DIR = "nube-bios/src/main/java/";
        Runner.runExample(JAVA_DIR, OsDeploymentVerticle.class);
    }

    @Override
    public void start() {
        super.start();
        System.out.println(this.getClass().getCanonicalName() + " Loader = " + OsDeploymentVerticle.class.getClassLoader());
        System.out.println("Current thread loader = " + Thread.currentThread().getContextClassLoader());
        System.out.println("Config on nube bios");
        System.out.println(Json.encodePrettily(config()));

        initializeJDBCClient()
            .flatMap(jdbcClient -> getConnection(jdbcClient)
                .map(conn -> conn.rxExecute(CREATE_TABLE))
                .flatMap(ignored -> setupMavenRepos())
                .flatMap(ignore -> loadOsFromMaven(jdbcClient)))
            .subscribe(
                ignore -> logger.info("Finished OS startup ..."),
                throwable -> logger.error("Error on OS startup ... \n" + throwable.getCause().getMessage()));
    }

    private Single<JDBCClient> initializeJDBCClient() {
        logger.info("Initializing JDBC client for local sqlite db");
        JsonObject jdbcConfig = new JsonObject();
        jdbcConfig.put("url", "jdbc:sqlite:nube-bios.db");
        jdbcConfig.put("driver_class", "org.sqlite.JDBC");
        return Single.just(JDBCClient.createNonShared(vertx, jdbcConfig));
    }

    private Single<SQLConnection> getConnection(SQLClient jdbcClient) {
        return jdbcClient.rxGetConnection()
            .flatMap(conn -> Single.just(conn)
                .doOnError(throwable -> logger.error("Cannot get connection object."))
                .doFinally(conn::close));
    }

    private Single<String> setupMavenRepos() {
        logger.info("Setting up maven local and remote repo");
        String local = System.getProperty("user.home") + "/.m2/repository";
        List<String> remotes = config().getJsonArray("remotes",
            new JsonArray()
                .add("http://192.168.1.68:8081/repository/maven-releases/")
                .add("http://192.168.1.68:8081/repository/maven-snapshots/")
                .add("http://192.168.1.68:8081/repository/maven-central/"))
            .getList();

        vertx.getDelegate().registerVerticleFactory(new MavenVerticleFactory(
            new ResolverOptions()
                .setLocalRepository(local)
                .setRemoteRepositories(remotes))
        );
        return Single.just("");
    }

    private Single<String> loadOsFromMaven(SQLClient jdbcClient) {
        // For us OS is the NubeAppInstaller, where we can find core logic
        logger.info("Loading Nube App Installer OS ...");

        return getConnection(jdbcClient)
            .flatMap(this::getInstalledOs)
            .flatMap(records -> {
                boolean autoInstall = config().getBoolean("autoInstall", true);
                if (records.size() == 0 && autoInstall) {
                    String version = "1.0-SNAPSHOT";
                    JsonObject options = new JsonObject();
                    return handleInstall(version, options, jdbcClient);
                } else {
                    JsonObject record = records.get(0);
                    String version = record.getString("version");
                    String optionsJson = record.getString("optionsJson");
                    String deploymentId = record.getString("deploymentId");
                    JsonObject options = new JsonObject(optionsJson);
                    return handleInstall(version, options, jdbcClient)
                        .flatMap(ignored -> getConnection(jdbcClient))
                        .flatMap(conn -> {
                            JsonArray params = new JsonArray(Collections.singletonList(deploymentId));
                            return conn.rxUpdateWithParams(DELETE_APP_INSTALLER_QUERY, params)
                                .map(ignored -> "");
                        });
                }
            });
    }

    private Single<String> handleInstall(String version, JsonObject options, SQLClient jdbcClient) {
        String serviceName = getServiceName(version);
        return vertx.rxDeployVerticle(serviceName)
            .flatMap(deploymentId -> {
                if (StringUtils.isNotNull(deploymentId)) {
                    logger.info("Successfully deployed the Nube App Installer.");
                    return saveData(deploymentId, version, options, jdbcClient); //persisting deployment info
                } else {
                    logger.info("Failed to deploy the Nube App Installer.");
                    throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), "");
                }
            });
    }

    private Single<String> saveData(String deploymentID, String version, JsonObject optionsJson, SQLClient jdbcClient) {
        JsonArray params = new JsonArray(Arrays.asList(deploymentID, version, Json.encode(optionsJson)));
        return getConnection(jdbcClient)
            .flatMap(conn -> conn.rxUpdateWithParams(INSERT_APP_INSTALLER_QUERY, params)
                .map(ignore -> {
                    logger.info("Persisting ", deploymentID, " for OS version ", version, "in database with config ", optionsJson);
                    return deploymentID;
                }));
    }

    private String getServiceName(String version) {
        return "maven:" + groupId + ":" + artifactId + ":" + version + "::" + service;
    }

    private Single<List<JsonObject>> getInstalledOs(SQLConnection conn) {
        return conn.rxQuery(SELECT_APP_INSTALLER_QUERY)
            .map(resultSet -> {
                logger.info("Installed OS: ", Json.encodePrettily(resultSet.getRows()));
                return resultSet.getRows();
            });
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
