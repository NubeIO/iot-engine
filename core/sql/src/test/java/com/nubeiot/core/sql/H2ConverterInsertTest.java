package com.nubeiot.core.sql;

import java.util.function.Function;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.reactivex.Single;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.sql.MockManyEntityHandler.MockManyNoData;
import com.nubeiot.core.sql.mock.manyschema.mock1.tables.pojos.TblSample_01;

@RunWith(VertxUnitRunner.class)
public class H2ConverterInsertTest extends BaseSqlDaoConverterTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseSqlTest.beforeSuite();
    }

    @Override
    protected Class<? extends MockManyEntityHandler> handler() {
        return MockManyNoData.class;
    }

    @Override
    protected Function<TblSample_01, Single<Integer>> manipulatePojo() {
        return p -> entityHandler.getSample01Dao().insert(p);
    }

}
