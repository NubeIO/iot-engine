package com.nubeiot.core.sql;

import org.jooq.SQLDialect;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.opentable.db.postgres.embedded.EmbeddedPostgres.Builder;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class PostgresMemTest extends BaseSqlTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Rule
    public SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance().customize(this::pgBuilder);

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Override
    public SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @Override
    @NonNull
    public String getJdbcUrl() {
        return pg.getEmbeddedPostgres().getJdbcUrl("postgres", "postgres");
    }

    @Test
    public void test_restart_app(TestContext context) {
        startSQL(context, OneSchema.CATALOG, MockOneEntityHandler.class);
        //        stopSQL(context);
        //        startSQL(OneSchema.CATALOG, MockOneEntityHandler.class, context);
    }

    private void pgBuilder(Builder builder) {
        //        builder.setDataDirectory(folder.getRoot().toPath().resolve("pgsql")).setCleanDataDirectory(true)
        //        .setPort(5432);
        builder.setCleanDataDirectory(true).setPort(10000);
    }

}
