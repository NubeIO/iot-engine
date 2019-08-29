package com.nubeiot.edge.module.datapoint;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointConfig.BuiltinData;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public class DataPointVerticleTest extends DynamicServiceTestBase {

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.DEBUG);
    }

    @Override
    protected DeploymentOptions getServiceOptions() {
        BuiltinData def = JsonData.from(MockData.data_Point_Setting_Tag(), BuiltinData.class);
        JsonObject sqlConfig = new JsonObject(
            "{\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString() + "\"}}");
        final JsonObject appConfig = new JsonObject().put(DataPointConfig.NAME, DataPointConfig.def(def).toJson())
                                                     .put(SqlConfig.NAME, sqlConfig);
        return new DeploymentOptions().setConfig(new JsonObject().put(AppConfig.NAME, appConfig));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected DataPointVerticle service() {
        return new DataPointVerticle();
    }

    @Test
    public void test_get_device(TestContext context) {
        final JsonObject syncConfig = new JsonObject("{\"type\":\"DITTO\",\"enabled\":false," +
                                                     "\"clientConfig\":{\"userAgent\":\"nubeio.edge.datapoint/1.0.0 " +
                                                     PrimaryKey.DEVICE + "\"}}");
        final JsonObject expected = JsonPojo.from(MockData.DEVICE)
                                            .toJson()
                                            .put("data_version", "0.0.2")
                                            .put("metadata", new JsonObject().put(DataSyncConfig.NAME, syncConfig));
        assertRestByClient(context, HttpMethod.GET, "/api/s/device/" + PrimaryKey.DEVICE, 200, expected,
                           new Customization("metadata.__data_sync__.clientConfig.hostInfo", (o1, o2) -> true),
                           new Customization("metadata.__data_sync__.clientConfig.options", (o1, o2) -> true));
    }

    @Test
    public void test_get_measure_unit(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/measure-unit", 200, MockData.MEASURE_UNITS,
                           JSONCompareMode.LENIENT);
    }

    @Test
    public void test_get_list_tags_by_point(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "/tags", 200,
                           new JsonObject("{\"tags\":[{\"id\":1,\"tag_name\":\"sensor\",\"tag_value\":\"temp\"}," +
                                          "{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}]}"));
    }

    @Test
    public void test_get_tag_by_point_and_id(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_GPIO_TEMP + "/tags/2", 200,
                           new JsonObject("{\"id\":2,\"tag_name\":\"source\",\"tag_value\":\"droplet\"}"));
    }

    @Test
    public void test_get_list_tags(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"tags\":[{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\"," +
            "\"tag_value\":\"temp\"},{\"id\":2,\"tag_name\":\"source\"," +
            "\"point\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\"," +
            "\"tag_value\":\"droplet\"},{\"id\":3,\"tag_name\":\"sensor\"," +
            "\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"tag_value\":\"temp\"},{\"id\":4,\"tag_name\":\"source\"," +
            "\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\",\"tag_value\":\"hvac\"}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/tags", 200, expected);
    }

    @Test
    public void test_get_tag_by_id(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"id\":1,\"tag_name\":\"sensor\",\"point\":\"1efaf662-1333-48d1-a60f-8fc60f259f0e\"," +
            "\"tag_value\":\"temp\"}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/tags/1", 200, expected);
    }

}
