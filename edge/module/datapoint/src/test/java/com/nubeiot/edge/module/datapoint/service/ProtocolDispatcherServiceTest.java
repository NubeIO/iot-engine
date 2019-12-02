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
            "{\"protocol_dispatchers\":[{\"id\":1,\"entity\":\"network\",\"action\":\"CREATE\"," +
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

}
