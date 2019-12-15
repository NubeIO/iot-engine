package com.nubeiot.core.sql;

import org.jooq.Catalog;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;

import io.reactivex.Single;

import com.nubeiot.core.event.EventMessage;

import lombok.NonNull;

/**
 * Represents for Schema handler.
 *
 * @since 1.0.0
 */
public interface SchemaHandler {

    /**
     * Defines {@code table} to check whether database is new or not.
     *
     * @return the table
     * @since 1.0.0
     */
    @NonNull Table table();

    /**
     * Check database is new or not. Normally just checking one specific table is existed or not.
     * <p>
     * Currently, it has not yet supported officially from {@code jooq}. So {@code NubeIO} supports 2 kinds: {@code H2}
     * and {@code PostgreSQL}. Other options, must be implemented by yourself.
     *
     * @param dsl the dsl context
     * @return {@code true} if new database, else otherwise
     * @see <a href="https://github.com/jOOQ/jOOQ/issues/8038">https://github.com/jOOQ/jOOQ/issues/8038</a>
     * @since 1.0.0
     */
    default boolean isNew(DSLContext dsl) {
        final SQLDialect dialect = dsl.configuration().family();
        if (dialect != SQLDialect.H2 && dialect != SQLDialect.POSTGRES) {
            return false;
        }
        String from = dialect == SQLDialect.H2 ? "information_schema.tables" : "pg_tables";
        String schema = dialect == SQLDialect.H2 ? "table_schema" : "schemaname";
        String tbl = dialect == SQLDialect.H2 ? "table_name" : "tablename";
        return 0 == dsl.selectCount()
                       .from(from)
                       .where(schema + " = '" + table().getSchema().getName() + "'")
                       .and(tbl + " = '" + table().getName() + "'")
                       .fetchOne(0, int.class);
    }

    /**
     * Declares schema initializer.
     *
     * @return the schema initializer
     * @since 1.0.0
     */
    @NonNull SchemaInitializer initializer();

    /**
     * Declares schema migrator.
     *
     * @return the schema migrator
     * @since 1.0.0
     */
    @NonNull SchemaMigrator migrator();

    /**
     * Do execute the initialization task or migration task.
     *
     * @param entityHandler the entity handler
     * @param catalog       the catalog
     * @return the event message in single
     * @see EntityHandler
     * @see #initializer()
     * @see #migrator()
     * @since 1.0.0
     */
    default @NonNull Single<EventMessage> execute(@NonNull EntityHandler entityHandler, @NonNull Catalog catalog) {
        if (isNew(entityHandler.dsl())) {
            return initializer().execute(entityHandler, catalog);
        }
        return migrator().execute(entityHandler, catalog);
    }

}
