package com.nubeiot.core.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.impl.DefaultConfiguration;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.exceptions.DatabaseException;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;
import com.zaxxer.hikari.HikariDataSource;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SQLWrapper implements IComponent {

    private static final Logger logger = LoggerFactory.getLogger(SQLWrapper.class);
    private static final String DDL_SQL_FILE = "ddl/ddl.ddl";

    private final Vertx vertx;
    private final SqlConfig sqlConfig;
    private final Supplier<Single<JsonObject>> initData;
    private DataSource dataSource;
    @Getter
    private Configuration jooqConfig;

    @Override
    public void start(Future<Void> startFuture) throws NubeException { }

    @Override
    public void start() throws NubeException {
        initDBConnection().flatMap(client -> this.createDatabase(client).flatMap(ignores -> this.initData.get()))
                          .subscribe(logger::info, throwable -> {
                              logger.error("Failed to startup application", throwable);
                              throw new IllegalStateException(ErrorMessage.parse(throwable).toJson().encode());
                          });
    }

    @Override
    public void stop() throws NubeException {
        try {
            this.dataSource.unwrap(HikariDataSource.class).close();
        } catch (SQLException e) {
            logger.debug("Unable to close datasource", e);
        }
    }

    @Override
    public void stop(Future<Void> future) throws NubeException { }

    private Single<SQLClient> initDBConnection() {
        logger.info("Create Hikari datasource from application configuration...");
        logger.debug(this.sqlConfig.getHikariConfig().toJson());
        this.dataSource = new HikariDataSource(this.sqlConfig.getHikariConfig());
        this.jooqConfig = new DefaultConfiguration().set(dataSource);
        return Single.just(JDBCClient.newInstance(io.vertx.ext.jdbc.JDBCClient.create(vertx, dataSource)));
    }

    private Single<List<Integer>> createDatabase(SQLClient sqlClient) {
        logger.info("Create database...");
        String fileContent = Strings.convertToString(Reflections.staticClassLoader().getResourceAsStream(DDL_SQL_FILE));
        if (Strings.isBlank(fileContent)) {
            return Single.just(new ArrayList<>());
        }
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

}
