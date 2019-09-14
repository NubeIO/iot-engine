package com.nubeiot.core.sql;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;

import org.jooq.Catalog;
import org.jooq.Configuration;
import org.jooq.Constraint;
import org.jooq.CreateIndexStep;
import org.jooq.CreateSchemaFinalStep;
import org.jooq.ForeignKey;
import org.jooq.Key;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DefaultConfiguration;

import io.reactivex.Single;
import io.vertx.core.Future;

import com.nubeiot.core.component.UnitVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.InitializerError.MigrationError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.zaxxer.hikari.HikariDataSource;

public final class SQLWrapper<T extends EntityHandler> extends UnitVerticle<SqlConfig, SqlContext<T>> {

    private final Catalog catalog;
    private DataSource dataSource;

    SQLWrapper(Catalog catalog, Class<T> handlerClass) {
        super(new SqlContext<>(handlerClass));
        this.catalog = catalog;
    }

    @Override
    public void start() {
        super.start();
        logger.info("Creating Hikari datasource from application configuration...");
        logger.debug(config.getHikariConfig().toJson());
        this.dataSource = new HikariDataSource(config.getHikariConfig());
    }

    @Override
    public void start(Future<Void> future) {
        this.start();
        this.createDatabase(new DefaultConfiguration().set(dataSource).set(config.getDialect()))
            .flatMap(this::validateInitOrMigrationData)
            .subscribe(result -> complete(future, result), t -> future.fail(NubeExceptionConverter.from(t)));
    }

    @Override
    public void stop() {
        try {
            this.dataSource.unwrap(HikariDataSource.class).close();
        } catch (SQLException e) {
            logger.info("Unable to close datasource", e);
        }
    }

    @Override
    public Class<SqlConfig> configClass() { return SqlConfig.class; }

    @Override
    public String configFile() { return "sql.json"; }

    private void complete(Future<Void> future, EventMessage result) {
        logger.info("Result: {}", result.toJson().encode());
        logger.info("DATABASE IS READY TO USE");
        future.complete();
    }

    private Single<EventMessage> createDatabase(Configuration jooqConfig) {
        try {
            EntityHandler handler = getContext().createHandler(jooqConfig, vertx).registerFunc(this::getSharedData);
            if (handler.isNew()) {
                createNewDatabase(jooqConfig);
                logger.info("Initializing data...");
                return handler.initData();
            }
            logger.info("Migrating database...");
            return handler.migrate();
        } catch (DataAccessException | NullPointerException | IllegalArgumentException | NubeException e) {
            return Single.error(new InitializerError("Unknown error when initializing database", e));
        }
    }

    private void createNewDatabase(Configuration jooqConfig) {
        logger.info("Creating database model...");
        logger.info("Creating schema...");
        this.catalog.schemaStream()
                    .map(schema -> createSchema(jooqConfig, schema))
                    .map(Schema::getTables)
                    .flatMap(Collection::stream)
                    .map(table -> createTableAndIndex(jooqConfig, table))
                    .map(this::listConstraint)
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, this::merge))
                    .forEach((table, constraints) -> createConstraints(jooqConfig, table, constraints));
        logger.info("Created database model successfully");
    }

    private Schema createSchema(Configuration jooqConfig, Schema schema) {
        try (CreateSchemaFinalStep step = jooqConfig.dsl().createSchemaIfNotExists(schema)) {
            logger.debug(step.getSQL());
            step.execute();
            logger.info("Created schema {} successfully", schema.getName());
            return schema;
        }
    }

    private Map<Table, Set<Constraint>> listConstraint(Table<?> table) {
        Set<Constraint> constraints = table.getKeys().stream().map(Key::constraint).collect(Collectors.toSet());
        if (Objects.nonNull(table.getPrimaryKey())) {
            constraints.add(table.getPrimaryKey().constraint());
        }
        if (Objects.nonNull(table.getReferences())) {
            constraints.addAll(table.getReferences().stream().map(ForeignKey::constraint).collect(Collectors.toSet()));
        }
        return Collections.singletonMap(table, constraints);
    }

    private Table<?> createTableAndIndex(Configuration jooqConfig, Table<?> table) {
        createTable(jooqConfig, table);
        createIndex(jooqConfig, table);
        logger.info("Created table {} successfully",
                    table.getSchema().getQualifiedName().append(table.getQualifiedName()));
        return table;
    }

    private void createTable(Configuration jooqConfig, Table<?> table) {
        logger.info("Creating table {}...", table.getSchema().getQualifiedName().append(table.getQualifiedName()));
        jooqConfig.dsl().createTableIfNotExists(table).columns(table.fields()).execute();
    }

    private void createIndex(Configuration jooqConfig, Table<?> table) {
        table.getIndexes().forEach(index -> {
            logger.debug("Creating index {}...", table.getSchema().getQualifiedName().append(index.getQualifiedName()));
            CreateIndexStep indexStep;
            if (index.getUnique()) {
                indexStep = jooqConfig.dsl().createUniqueIndexIfNotExists(index.getName());
            } else {
                indexStep = jooqConfig.dsl().createIndexIfNotExists(index.getName());
            }
            indexStep.on(table, index.getFields()).where(index.getWhere()).execute();
        });
    }

    private void createConstraints(Configuration jooqConfig, Table table, Set<Constraint> constraints) {
        logger.info("Creating constraints of table {}...", table.getName());
        jooqConfig.dsl().setSchema(table.getSchema()).execute();
        constraints.forEach(constraint -> {
            logger.debug("Constraint: {}", constraint.getQualifiedName());
            jooqConfig.dsl().alterTable(table).add(constraint).execute();
        });
    }

    private Set<Constraint> merge(Set<Constraint> c1, Set<Constraint> c2) {
        return Stream.of(c1, c2).flatMap(Set::stream).collect(Collectors.toSet());
    }

    private Single<EventMessage> validateInitOrMigrationData(EventMessage result) {
        if (result.isError()) {
            ErrorMessage error = result.getError();
            Throwable t = error.getThrowable();
            if (Objects.isNull(t)) {
                t = new NubeException(error.getCode(), error.getMessage());
            }
            if (result.getAction() == EventAction.INIT) {
                return Single.error(new InitializerError("Failed to startup SQL component", t));
            } else {
                return Single.error(new MigrationError("Failed to startup SQL component", t));
            }
        }
        return Single.just(result);
    }

}
