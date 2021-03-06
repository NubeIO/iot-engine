package com.nubeiot.core.sql;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Configs;

public class SqlConfigTest {

    @Test
    public void test_default() throws JSONException {
        SqlConfig sqlConfig = new SqlConfig();
        SqlConfig from = IConfig.from(Configs.loadJsonConfig("sql.json"), SqlConfig.class);
        System.out.println("DEFAULT: " + sqlConfig.toJson());
        System.out.println("FROM: " + from.toJson());
        JSONAssert.assertEquals(sqlConfig.toJson().encode(), from.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_hikari() {
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.getHikariConfig().setJdbcUrl("abc");
        System.out.println(sqlConfig.toJson());
        SqlConfig from = IConfig.from(sqlConfig.toJson(), SqlConfig.class);
        Assert.assertEquals("abc", from.getHikariConfig().getJdbcUrl());
    }

}
