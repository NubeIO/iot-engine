package com.nubeiot.core.sql;

import org.jooq.DSLContext;

import io.vertx.ext.unit.TestContext;

abstract class BaseSqlDslConverterTest extends BaseSqlConverterTest {

    protected DSLContext dsl;

    @Override
    protected void initData(TestContext context) {
        this.dsl = entityHandler.getJooqConfig().dsl();
        //id = 1, f_date_1 = 2019-02-17
        //f_timestamp = 2019-02-17 23:59:59, f_timestampz = 2019-02-17 23:59:59
        //f_duration = PT50H30M20S, id = P2Y3M4W5D
        this.dsl.execute(
            "INSERT INTO tbl_sample_01 (id, f_date_1, f_timestamp, f_timestampz, f_time, f_duration, f_period) " +
            "VALUES (1, PARSEDATETIME('2019-02-17', 'yyyy-MM-dd'), PARSEDATETIME('2019-02-17" +
            " 23:59:59', 'yyyy-MM-dd HH:mm:ss'), PARSEDATETIME('2019-02-17 23:59:59', 'yyyy-MM-dd " +
            "HH:mm:ss'), PARSEDATETIME('23:59:59', 'HH:mm:ss'), 'PT50H30M20S', 'P2Y3M4W5D');");

        //id = 2, f_date_1 = 2019-02-17
        //f_timestamp = 2019-02-17 00:00:01, f_timestampz = 2019-02-17 00:00:01
        //f_duration = PT50H30M20S, id = P2Y3M4W5D
        this.dsl.execute("INSERT INTO tbl_sample_01 (id, f_date_1, f_timestamp, f_timestampz, f_time, f_duration, " +
                         "f_period) VALUES (2, PARSEDATETIME('2019-02-17', 'yyyy-MM-dd'), PARSEDATETIME('2019-02-17" +
                         " 00:00:01', 'yyyy-MM-dd HH:mm:ss'), PARSEDATETIME('2019-02-17 00:00:01', 'yyyy-MM-dd " +
                         "HH:mm:ss'), PARSEDATETIME('00:00:01', 'HH:mm:ss'), 'PT50H30M20S', 'P2Y3M4W5D');");
    }

}
