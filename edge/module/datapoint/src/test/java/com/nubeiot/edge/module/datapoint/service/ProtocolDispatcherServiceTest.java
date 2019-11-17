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
            "{\"protocol_dispatchers\":[{\"id\":1,\"protocol\":\"BACNET\",\"entity\":\"network\",\"address\":\"bacnet" +
            ".dispatcher.network\"},{\"id\":2,\"protocol\":\"BACNET\",\"entity\":\"equipment\",\"address\":\"bacnet" +
            ".dispatcher.equipment\"},{\"id\":3,\"protocol\":\"BACNET\",\"entity\":\"point\",\"address\":\"bacnet" +
            ".dispatcher.point\"}]}");
        asserter(context, true, expected, ProtocolDispatcherService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

}
