package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.github.zero.utils.UUID64;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class HistorySettingServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_history_setting_by_point(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"type\":\"COV\",\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"enabled\":false,\"tolerance\":1.0}");
        asserter(context, true, expected, HistorySettingService.class.getName(), EventAction.GET_ONE,
                 RequestData.builder()
                            .body(new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_GPIO_TEMP)))
                            .build());
    }

    @Test
    public void test_get_history_setting_by_point_not_found(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with point_id=" +
                                                                    PrimaryKey.P_BACNET_SWITCH);
        asserter(context, false, expected, HistorySettingService.class.getName(), EventAction.GET_ONE,
                 RequestData.builder()
                            .body(new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_BACNET_SWITCH)))
                            .build());
    }

    @Test
    public void test_create_or_update_history_setting_already_existed(TestContext context) {
        final JsonObject resource = new JsonObject(
            "{\"type\":\"COV\",\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"enabled\":true,\"tolerance\":1.0}");
        final JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", resource);
        asserter(context, true, expected, HistorySettingService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder()
                            .body(new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_GPIO_TEMP))
                                                  .put("enabled", true))
                            .build());
    }

    @Test
    public void test_create_or_update_history_setting_not_yet_existed(TestContext context) {
        final JsonObject resource = new JsonObject("{\"type\":\"COV\",\"point\":\"" + PrimaryKey.P_BACNET_TEMP +
                                                   "\",\"enabled\":true,\"tolerance\":1.0,\"schedule\":null}");
        final JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", resource);
        asserter(context, true, expected, HistorySettingService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder()
                            .body(new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_BACNET_TEMP))
                                                  .put("enabled", true)
                                                  .put("type", "COV")
                                                  .put("tolerance", 1.0))
                            .build());
    }

    @Test
    public void test_create_or_update_history_setting_missing_type(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message",
                                                         "History setting type is mandatory. One of: [COV, PERIOD]");
        asserter(context, false, expected, HistorySettingService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder()
                            .body(new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_BACNET_TEMP))
                                                  .put("enabled", true))
                            .build());
    }

    @Test
    public void test_create_or_update_history_setting_invalid_cov(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message",
                                                         "History setting tolerance must be positive number");
        asserter(context, false, expected, HistorySettingService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder()
                            .body(new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_GPIO_TEMP))
                                                  .put("enabled", true)
                                                  .put("type", "COV")
                                                  .put("tolerance", -1.0))
                            .build());
    }

    @Test
    public void test_create_or_update_history_setting_missing_cov(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "History setting tolerance is mandatory");
        asserter(context, false, expected, HistorySettingService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder()
                            .body(new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_BACNET_TEMP))
                                                  .put("enabled", true)
                                                  .put("type", "COV"))
                            .build());
    }

}
