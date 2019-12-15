package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.ProtocolDispatcherAddress;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

public class ProtocolDispatcherServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Protocol_Dispatcher();
    }

    @Test
    public void test_get_list_protocols(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"dispatchers\":[{\"id\":1,\"entity\":\"network\",\"action\":\"CREATE\"," +
            "\"protocol\":\"BACNET\",\"address\":\"bacnet.dispatcher.network\",\"global\":false," +
            "\"state\":\"ENABLED\"},{\"id\":2,\"entity\":\"network\",\"action\":\"REMOVE\",\"protocol\":\"BACNET\"," +
            "\"address\":\"bacnet.dispatcher.network\",\"global\":false,\"state\":\"ENABLED\"},{\"id\":3," +
            "\"entity\":\"device\",\"action\":\"CREATE\",\"protocol\":\"BACNET\",\"address\":\"bacnet.dispatcher" +
            ".device\",\"global\":false,\"state\":\"DISABLED\"},{\"id\":4,\"entity\":\"point\",\"action\":\"CREATE\"," +
            "\"protocol\":\"BACNET\",\"address\":\"bacnet.dispatcher.point\",\"global\":false,\"state\":\"ENABLED\"}," +
            "{\"id\":5,\"entity\":\"tag\",\"action\":\"CREATE\",\"protocol\":\"BACNET\",\"address\":\"bacnet" +
            ".dispatcher.tag\",\"global\":true,\"state\":\"ENABLED\"}]}");
        asserter(context, true, expected, ProtocolDispatcherService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

    @Test
    public void test_create_unsupported_event(TestContext context) {
        final JsonObject expected = new JsonObject().put("message", "Unsupported event " + EventAction.CREATE)
                                                    .put("code", ErrorCode.STATE_ERROR);
        asserter(context, false, expected, ProtocolDispatcherService.class.getName(), EventAction.CREATE,
                 RequestData.builder().build());
    }

    @Test
    public void test_create_or_update_existed_resource(TestContext context) {
        final ProtocolDispatcher dispatcher = new ProtocolDispatcher().setProtocol(Protocol.BACNET)
                                                                      .setAction(EventAction.CREATE)
                                                                      .setEntity(
                                                                          NetworkMetadata.INSTANCE.singularKeyName())
                                                                      .setAddress(ProtocolDispatcherAddress.NETWORK)
                                                                      .setState(State.DISABLED);
        final JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", JsonPojo.from(dispatcher)
                                                                             .toJson()
                                                                             .put("id", 1)
                                                                             .put("global", false));
        asserter(context, true, expected, ProtocolDispatcherService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder().body(JsonPojo.from(dispatcher).toJson()).build());
    }

    @Test
    public void test_create_or_update_new_resource(TestContext context) {
        final ProtocolDispatcher dispatcher = new ProtocolDispatcher().setProtocol(Protocol.MODBUS)
                                                                      .setAction(EventAction.CREATE)
                                                                      .setEntity(
                                                                          PointValueMetadata.INSTANCE.singularKeyName())
                                                                      .setAddress(ProtocolDispatcherAddress.NETWORK)
                                                                      .setState(State.ENABLED);
        final JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", JsonPojo.from(dispatcher)
                                                                             .toJson()
                                                                             .put("id", 6)
                                                                             .put("global", false));
        asserter(context, true, expected, ProtocolDispatcherService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder().body(JsonPojo.from(dispatcher).toJson()).build());
    }

    @Test
    public void test_create_or_update_invalid_resource(TestContext context) {
        final ProtocolDispatcher dispatcher = new ProtocolDispatcher().setAction(EventAction.CREATE)
                                                                      .setAddress(ProtocolDispatcherAddress.NETWORK)
                                                                      .setState(State.ENABLED);
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Missing dispatcher entity");
        asserter(context, false, expected, ProtocolDispatcherService.class.getName(), EventAction.CREATE_OR_UPDATE,
                 RequestData.builder().body(JsonPojo.from(dispatcher).toJson()).build());
    }

}
