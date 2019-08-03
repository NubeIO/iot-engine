package com.nubeiot.core.sql;

import java.util.function.Function;

import org.jooq.SQLDialect;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;

import io.reactivex.Single;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;
import com.opentable.db.postgres.embedded.EmbeddedPostgres.Builder;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class PostgresConverterTest extends BaseSqlConverterTest {

    @Rule
    public SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance().customize(this::pgBuilder);

    private void pgBuilder(Builder builder) {
        builder.setCleanDataDirectory(true).setPort(10001);
    }

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    protected Class<? extends MockManyEntityHandler> handler() {
        return MockManyNoData.class;
    }

    @Override
    protected Function<TblSample_01, Single<Integer>> function() {
        return p -> entityHandler.getSample01Dao().insert(p);
    }

    @Override
    SQLDialect getDialect() {
        return SQLDialect.POSTGRES;
    }

    @Override
    @NonNull String getJdbcUrl() {
        return pg.getEmbeddedPostgres().getJdbcUrl("postgres", "postgres");
    }

}
