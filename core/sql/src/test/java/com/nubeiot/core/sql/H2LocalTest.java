package com.nubeiot.core.sql;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.InitializerError.MigrationError;
import com.nubeiot.core.sql.MockManyEntityHandler.MockManyErrorData;
import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class H2LocalTest extends BaseSqlTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Override
    @NonNull
    public String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("dbh2local").toString();
    }

    @Test
    public void test_restart_app(TestContext context) {
        startSQL(context, SchemaTest.ManySchema.CATALOG, MockManyNoData.class);
        stopSQL(context);
        startSQL(context, SchemaTest.ManySchema.CATALOG, MockManyNoData.class);
    }

    @Test
    public void test_init_failed(TestContext context) {
        startSQLFailed(context, SchemaTest.ManySchema.CATALOG, MockManyErrorData.class,
                       t -> context.assertTrue(t instanceof InitializerError));
    }

    @Test
    public void test_migrate_failed(TestContext context) {
        startSQL(context, SchemaTest.ManySchema.CATALOG, MockManyNoData.class);
        stopSQL(context);
        startSQLFailed(context, SchemaTest.ManySchema.CATALOG, MockManyErrorData.class,
                       t -> context.assertTrue(t instanceof MigrationError));
    }

}
