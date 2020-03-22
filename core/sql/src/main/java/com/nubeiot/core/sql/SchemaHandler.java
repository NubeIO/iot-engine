package com.nubeiot.core.sql;

import java.util.Optional;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ErrorData;

import lombok.NonNull;

/**
 * Represents for Schema handler.
 *
 * @since 1.0.0
 */
public interface SchemaHandler {

    String READINESS_ADDRESS = "SCHEMA_READINESS_ADDRESS";

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
     * Declares readiness address for notification after {@link #execute(EntityHandler)}.
     *
     * @param entityHandler given handler for helping lookup dynamic {@code address}
     * @return readiness address
     * @since 1.0.0
     */
    default @NonNull String readinessAddress(@NonNull EntityHandler entityHandler) {
        return Optional.ofNullable((String) entityHandler.sharedData(READINESS_ADDRESS))
                       .orElse(this.getClass().getName() + ".readiness");
    }

    /**
     * Do execute the initialization task or migration task.
     *
     * @param entityHandler the entity handler
     * @return the event message in single
     * @see EntityHandler
     * @see #initializer()
     * @see #migrator()
     * @since 1.0.0
     */
    default @NonNull Single<EventMessage> execute(@NonNull EntityHandler entityHandler) {
        final Single<EventMessage> result = isNew(entityHandler.dsl())
                                            ? initializer().execute(entityHandler)
                                            : migrator().execute(entityHandler);
        final EventbusClient c = entityHandler.eventClient();
        final String address = readinessAddress(entityHandler);
        return result.doOnError(t -> c.publish(address, EventMessage.error(EventAction.NOTIFY_ERROR,
                                                                           ErrorData.builder().throwable(t).build())))
                     .doOnSuccess(msg -> {
                         final JsonObject headers = new JsonObject().put("status", msg.getStatus())
                                                                    .put("action", msg.getAction());
                         final RequestData reqData = RequestData.builder().body(msg.getData()).headers(headers).build();
                         c.publish(address, EventMessage.initial(EventAction.NOTIFY, reqData));
                     });
    }

}
