package com.nubeiot.edge.module.datapoint.sync;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.auth.BasicCredential;
import com.nubeiot.auth.Credential.CredentialType;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.ExpectedResponse;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.PointCategory;
import com.nubeiot.iotdata.dto.PointKind;
import com.nubeiot.iotdata.dto.PointType;
import com.nubeiot.iotdata.edge.model.tables.pojos.Device;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class DittoHttpSyncTest extends BaseDataPointVerticleTest {

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("org.jooq")).setLevel(Level.DEBUG);
    }

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Override
    protected DataSyncConfig syncConfig() {
        return DataSyncConfig.enabled(new BasicCredential(CredentialType.BASIC, "sandbox", "111"),
                                      HostInfo.builder().host("localhost").port(7000).ssl(false).build());
    }

    @Test
    public void test_patch_device(TestContext context) {
        Async async = context.async();
        JsonObject data = new JsonObject("{\"id\":\"" + PrimaryKey.DEVICE + "\",\"code\":\"TET_01\"," +
                                         "\"customer_code\":\"NUBEIO\",\"site_code\":\"SYDNEY-00001\"," +
                                         "\"data_version\":\"0.0.3\",\"metadata\":{\"hostname\":\"abc\"}}");
        JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Device p1 = new Device().setCode("TET_01")
                                .setDataVersion("0.0.3")
                                .setMetadata(new JsonObject().put("hostname", "abc"));
        final Consumer<ResponseData> after = r -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                TestHelper.testComplete(async);
            }
        };
        RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson()).build();
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE), req,
                           ExpectedResponse.builder().code(200).expected(expected).after(after).build());
    }

    @Test
    public void test_create_point(TestContext context) {
        Async async = context.async();
        final UUID id = UUID.randomUUID();
        JsonObject data = new JsonObject(
            "{\"id\":\"" + id + "\",\"code\":\"TET_01\",\"kind\":\"OUTPUT\",\"type\":\"DIGITAL\",\"category" +
            "\":\"BACNET\",\"unit\":{\"type\":\"fahrenheit\",\"symbol\":\"°F\",\"category\":\"TEMPERATURE\"}," +
            "\"device\":\"" + PrimaryKey.DEVICE + "\",\"enabled\":true}");
        JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                              .put("status", Status.SUCCESS)
                                              .put("resource", data);
        Point p1 = new Point().setId(id)
                              .setCode("TET_01")
                              .setKind(PointKind.OUTPUT)
                              .setType(PointType.DIGITAL)
                              .setCategory(PointCategory.BACNET)
                              .setMeasureUnit(Temperature.FAHRENHEIT.type());
        final Consumer<ResponseData> after = r -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                TestHelper.testComplete(async);
            }
        };
        RequestData req = RequestData.builder().body(JsonPojo.from(p1).toJson()).build();
        assertRestByClient(context, HttpMethod.POST,
                           "/api/s/device/" + UUID64.uuidToBase64(PrimaryKey.DEVICE) + "/point", req,
                           ExpectedResponse.builder().code(201).expected(expected).after(after).build());
    }

    @Test
    public void test_create_point_data(TestContext context) throws InterruptedException {
        Async async = context.async(2);
        CountDownLatch latch = new CountDownLatch(2);
        RequestData req = RequestData.builder()
                                     .body(JsonPojo.from(new PointValueData().setPriority(5).setValue(24d)).toJson())
                                     .build();
        JsonObject data = new JsonObject("{\"point\":\"" + PrimaryKey.P_BACNET_SWITCH + "\",\"value\":24.0," +
                                         "\"priority\":5,\"priority_values\":{\"5\":24.0}}");
        final Consumer<ResponseData> after = r -> {
            try {
                latch.countDown();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                TestHelper.testComplete(async);
            }
        };
        final ExpectedResponse resp = ExpectedResponse.builder()
                                                      .code(201)
                                                      .expected(new JsonObject().put("action", EventAction.CREATE)
                                                                                .put("status", Status.SUCCESS)
                                                                                .put("resource", data))
                                                      .after(after)
                                                      .build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/point/" + PrimaryKey.P_BACNET_SWITCH + "/data", req, resp);

        latch.await(2500, TimeUnit.MILLISECONDS);
        RequestData req1 = RequestData.builder()
                                      .body(JsonPojo.from(new PointValueData().setPriority(9).setValue(29d)).toJson())
                                      .build();
        JsonObject data1 = new JsonObject("{\"point\":\"" + PrimaryKey.P_BACNET_SWITCH + "\",\"value\":24.0," +
                                          "\"priority\":5,\"priority_values\":{\"5\":24.0,\"9\":29.0}," +
                                          "\"time_audit\":{\"created_time\":\"\"," +
                                          "\"created_by\":\"UNDEFINED\",\"last_modified_time\":\"\"," +
                                          "\"last_modified_by\":\"UNDEFINED\",\"revision\":2}," +
                                          "\"sync_audit\":{\"status\":\"INITIAL\"," +
                                          "\"data\":{\"message\":\"Not yet synced modified resource with record " +
                                          "revision 2\"}}}");
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/point/" + PrimaryKey.P_BACNET_SWITCH + "/data?_audit",
                           req1,
                           ExpectedResponse.builder()
                                           .code(200)
                                           .expected(new JsonObject().put("action", EventAction.PATCH)
                                                                     .put("status", Status.SUCCESS)
                                                                     .put("resource", data1))
                                           .customizations(IGNORE.apply("resource.time_audit.created_time"),
                                                           IGNORE.apply("resource.time_audit.last_modified_time"),
                                                           IGNORE.apply("resource.sync_audit.last_success_time"),
                                                           IGNORE.apply("resource.sync_audit.last_success_message" +
                                                                        ".time_audit.created_time"))
                                           .after(after)
                                           .build());
    }

}
