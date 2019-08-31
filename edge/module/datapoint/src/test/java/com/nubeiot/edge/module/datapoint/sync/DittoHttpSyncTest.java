package com.nubeiot.edge.module.datapoint.sync;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Test;

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
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

public class DittoHttpSyncTest extends BaseDataPointVerticleTest {

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
                                         "\"customer_code\":\"XXX\",\"site_code\":\"XXX-00001\",\"data_version\":\"0" +
                                         ".0.3\",\"metadata\":{\"hostname\":\"abc\"}}");
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

}
