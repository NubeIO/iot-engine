package com.nubeiot.core.sql;

import org.jooq.SQLDialect;
import org.junit.After;
import org.junit.Before;
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

    private void pgBuilder(Builder builder) {
        //        builder.setDataDirectory(folder.getRoot().toPath().resolve("pgsql")).setCleanDataDirectory(true)
        //        .setPort(5432);
        builder.setCleanDataDirectory(true).setPort(5555);
    }

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @Override
    @NonNull String getJdbcUrl() {
        return pg.getEmbeddedPostgres().getJdbcUrl("postgres", "postgres");
    }

    @Test
    public void test_restart_app(TestContext context) throws InterruptedException {
        startSQL(OneSchema.CATALOG, MockOneEntityHandler.class, context);
        //        stopSQL(context);
        //        startSQL(OneSchema.CATALOG, MockOneEntityHandler.class, context);
    }

}
