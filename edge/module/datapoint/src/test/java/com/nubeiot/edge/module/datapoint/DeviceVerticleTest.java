package com.nubeiot.edge.module.datapoint;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.ExpectedResponse;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class DeviceVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Device_Network();
    }

    @Test
    public void test_get_device(TestContext context) {
        final JsonObject syncConfig = new JsonObject("{\"type\":\"DITTO\",\"enabled\":false," +
                                                     "\"clientConfig\":{\"userAgent\":\"nubeio.edge.datapoint/1.0.0 " +
                                                     UUID64.uuidToBase64(PrimaryKey.DEVICE) + "\",\"hostInfo\":{}," +
                                                     "\"options\":{}}}");
        final JsonObject expected = JsonPojo.from(MockData.DEVICE)
                                            .toJson()
                                            .put("data_version", "0.0.2")
                                            .put("metadata", new JsonObject().put(DataSyncConfig.NAME, syncConfig));
        assertRestByClient(context, HttpMethod.GET, "/api/s/device/" + PrimaryKey.DEVICE, 200, expected,
                           JsonHelper.ignore("metadata.__data_sync__.clientConfig.hostInfo"),
                           JsonHelper.ignore("metadata.__data_sync__.clientConfig.options"));
    }

    @Test
    public void test_get_measure_unit(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/measure-unit", 200, MockData.MEASURE_UNITS,
                           JSONCompareMode.LENIENT);
    }

    @Test
    public void test_patch_device_readOnly_field(TestContext context) {
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/device/" + PrimaryKey.DEVICE,
                           RequestData.builder().body(new JsonObject().put("customer_code", "123")).build(),
                           ExpectedResponse.builder()
                                           .code(400)
                                           .expected(new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                                     .put("message", "Customer code is read-only"))
                                           .build());
    }

}
