package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero88.utils.UUID64;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

public class NetworkServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Edge_Network();
    }

    @Test
    public void test_list_with_implicit_edge(TestContext context) {
        listThenAssert(context, RequestData.builder().build());
    }

    @Test
    public void test_list_with_explicit_edge(TestContext context) {
        listThenAssert(context, RequestData.builder()
                                           .body(new JsonObject().put("edge_id", UUID64.uuidToBase64(PrimaryKey.EDGE)))
                                           .build());
    }

    @Test
    public void test_get_by_implicit_edge(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.searchNetwork(PrimaryKey.DEFAULT_NETWORK).setEdge(null)).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("network_id",
                                                                UUID64.uuidToBase64(PrimaryKey.DEFAULT_NETWORK)))
                                     .build();
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_by_explicit_edge(TestContext context) {
        JsonObject expected = JsonPojo.from(MockData.searchNetwork(PrimaryKey.BACNET_NETWORK).setEdge(null)).toJson();
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("edge_id", PrimaryKey.EDGE.toString())
                                                           .put("network_id", PrimaryKey.BACNET_NETWORK.toString()))
                                     .build();
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_create_implicit_edge(TestContext context) {
        final Network network = MockData.searchNetwork(PrimaryKey.DEFAULT_NETWORK)
                                        .setId(UUID.randomUUID())
                                        .setProtocol(Protocol.UNKNOWN)
                                        .setCode("TEST-1")
                                        .setState(State.DISABLED);
        createThenAssert(context, network, new Network(network).setProtocol(null).setEdge(null));
    }

    @Test
    public void test_create_explicit_edge(TestContext context) {
        final Network network = MockData.searchNetwork(PrimaryKey.DEFAULT_NETWORK)
                                        .setId(UUID.randomUUID())
                                        .setProtocol(Protocol.MODBUS)
                                        .setCode("TEST-2")
                                        .setState(State.UNAVAILABLE);
        createThenAssert(context, network, new Network(network));
    }

    @Test
    public void test_create_invalid_edge(TestContext context) {
        final UUID invalidEdge = UUID.randomUUID();
        final Network network = MockData.searchNetwork(PrimaryKey.DEFAULT_NETWORK)
                                        .setId(UUID.randomUUID())
                                        .setCode("TEST-2")
                                        .setEdge(invalidEdge);
        final RequestData req = RequestData.builder().body(JsonPojo.from(network).toJson()).build();
        final JsonObject expected = new JsonObject().put("code", "NOT_FOUND")
                                                    .put("message", "Not found resource with edge_id=" + invalidEdge);
        asserter(context, false, expected, NetworkService.class.getName(), EventAction.CREATE, req);
    }

    private void createThenAssert(TestContext context, Network response, Network request) {
        final JsonObject expected = createResponseExpected(response);
        final RequestData req = RequestData.builder().body(JsonPojo.from(request).toJson()).build();
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.CREATE, req);
    }

    private void listThenAssert(TestContext context, RequestData reqData) {
        final JsonArray networks = MockData.NETWORKS.stream()
                                                    .map(n -> JsonPojo.from(new Network(n).setEdge(null)).toJson())
                                                    .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        final JsonObject expected = new JsonObject().put("networks", networks);
        asserter(context, true, expected, NetworkService.class.getName(), EventAction.GET_LIST, reqData,
                 JSONCompareMode.LENIENT);
    }

    private JsonObject createResponseExpected(Network response) {
        return new JsonObject().put("action", EventAction.CREATE)
                               .put("status", Status.SUCCESS)
                               .put("resource",
                                    JsonPojo.from(response).toJson(JsonData.MAPPER, EntityTransformer.AUDIT_FIELDS));
    }

}
