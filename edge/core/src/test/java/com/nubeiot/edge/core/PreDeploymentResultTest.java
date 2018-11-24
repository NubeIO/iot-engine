package com.nubeiot.edge.core;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventType;
import com.nubeiot.edge.core.PreDeploymentResult;

import io.vertx.core.json.JsonObject;

public class PreDeploymentResultTest {

    @Test
    public void test_toJson_NonNull() {
        final JsonObject jsonObject = new PreDeploymentResult("1", EventType.REMOVE, State.ENABLED, "serviceId",
                                                              "deployId",
                                                              Collections.singletonMap("testAbc", "ab")).toJson();
        Assert.assertEquals("{\"transaction_id\":\"1\",\"event\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                            "\"service_id\":\"serviceId\",\"deploy_id\":\"deployId\"," +
                            "\"deploy_cfg\":{\"testAbc\":\"ab\"}}", jsonObject.encode());
    }

    @Test
    public void test_toJson_Null() {
        final JsonObject jsonObject = new PreDeploymentResult("1", EventType.REMOVE, State.ENABLED, "serviceId", null,
                                                              null).toJson();
        Assert.assertEquals("{\"transaction_id\":\"1\",\"event\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                            "\"service_id\":\"serviceId\"}", jsonObject.encode());
    }

}