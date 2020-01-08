package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public class PointValueVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_pv_directly(TestContext context) {
        final PointValueData v = MockData.searchData(PrimaryKey.P_GPIO_HUMIDITY);
        assertRestByClient(context, HttpMethod.GET, "/api/s/data/" + PrimaryKey.P_GPIO_HUMIDITY, 200,
                           JsonPojo.from(v).toJson(JsonData.MAPPER, EntityTransformer.AUDIT_FIELDS));
    }

    @Test
    public void test_get_pv_via_point(TestContext context) {
        final PointValueData v = MockData.searchData(PrimaryKey.P_BACNET_FAN);
        assertRestByClient(context, HttpMethod.GET, "/api/s/point/" + PrimaryKey.P_BACNET_FAN + "/data", 200,
                           JsonPojo.from(v).toJson(JsonData.MAPPER, EntityTransformer.AUDIT_FIELDS));
    }

    @Test
    public void test_create_pv_already_existed_200(TestContext context) {
        final PointValueData v = MockData.searchData(PrimaryKey.P_GPIO_HUMIDITY).setPriority(2).setValue(15.0);
        v.getPriorityValues().add(2, 15);
        final JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", JsonPojo.from(v)
                                                                             .toJson(JsonData.MAPPER,
                                                                                     EntityTransformer.AUDIT_FIELDS));
        final JsonObject body = new JsonObject().put("value", 15.0)
                                                .put("priority", 2)
                                                .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString());
        assertRestByClient(context, HttpMethod.PUT, "/api/s/point/" + PrimaryKey.P_GPIO_HUMIDITY + "/data",
                           RequestData.builder().body(body).build(), 200, expected);
    }

}
