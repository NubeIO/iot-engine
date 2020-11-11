package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.github.zero88.utils.UUID64;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestFilter.Filters;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

public class NetworkTransducerForceDeleteTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_delete_force_default_network_unsuccessful(TestContext context) {
        final JsonObject network = new JsonObject().put("network_id", UUID64.uuidToBase64(PrimaryKey.DEFAULT_NETWORK));
        final RequestData req = RequestData.builder()
                                           .body(network)
                                           .filter(new JsonObject().put(Filters.FORCE, true))
                                           .build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INSUFFICIENT_PERMISSION_ERROR)
                                                    .put("message", "Network code DEFAULT is read-only");
        asserter(context, false, expected, NetworkService.class.getName(), EventAction.REMOVE, req);
    }

    @Test
    public void test_delete_another_network_unsuccessful(TestContext context) {
        final String nwId = UUID64.uuidToBase64(PrimaryKey.BACNET_NETWORK);
        final RequestData req = RequestData.builder().body(new JsonObject().put("network_id", nwId)).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.BEING_USED)
                                                    .put("message",
                                                         "Resource with network_id=" + PrimaryKey.BACNET_NETWORK +
                                                         " is using by another resource");
        asserter(context, false, expected, NetworkService.class.getName(), EventAction.REMOVE, req);
    }

    @Test
    public void test_delete_force_another_network_successful(TestContext context) {
        final JsonObject nwReq = new JsonObject().put("network_id", UUID64.uuidToBase64(PrimaryKey.BACNET_NETWORK));
        final RequestData req = RequestData.builder()
                                           .body(nwReq)
                                           .filter(new JsonObject().put(Filters.FORCE, true))
                                           .build();
        final Network network = MockData.searchNetwork(PrimaryKey.BACNET_NETWORK);
        final JsonObject value = JsonPojo.from(network).toJson(JsonData.MAPPER, EntityTransformer.AUDIT_FIELDS);
        value.remove("edge");
        final JsonObject expected = new JsonObject().put("action", EventAction.REMOVE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", value);
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.REMOVE, req);
    }

}
