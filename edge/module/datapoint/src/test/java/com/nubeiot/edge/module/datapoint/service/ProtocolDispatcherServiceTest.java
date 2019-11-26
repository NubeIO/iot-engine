package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;

public class ProtocolDispatcherServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Protocol_Dispatcher();
    }

    @Test
    public void test_get_list_protocols(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"protocol_dispatchers\":[{\"id\":1,\"protocol\":\"BACNET\",\"entity\":\"network\"," +
            "\"action\":\"CREATE\",\"address\":\"bacnet.dispatcher.network\",\"global\":false},{\"id\":2," +
            "\"protocol\":\"BACNET\",\"entity\":\"device\",\"action\":\"CREATE\",\"address\":\"bacnet.dispatcher" +
            ".device\",\"global\":false},{\"id\":3,\"protocol\":\"BACNET\",\"entity\":\"point\"," +
            "\"action\":\"CREATE\",\"address\":\"bacnet.dispatcher.point\",\"global\":false}]}");
        asserter(context, true, expected, ProtocolDispatcherService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

}
