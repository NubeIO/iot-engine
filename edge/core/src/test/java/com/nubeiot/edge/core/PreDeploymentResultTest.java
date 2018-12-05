package com.nubeiot.edge.core;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventType;

import io.vertx.core.json.JsonObject;

public class PreDeploymentResultTest {

    @Test
    public void test_toJson_NonNull() {
        final JsonObject jsonObject = new PreDeploymentResult("1", EventType.REMOVE, State.ENABLED, "serviceId",
                                                              "deployId",
                                                              Collections.singletonMap("testAbc", "ab")).toJson();
        System.out.println(jsonObject);
        Assert.assertEquals("{\"transaction_id\":\"1\",\"event\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                            "\"service_id\":\"serviceId\",\"deploy_id\":\"deployId\"," +
                            "\"deploy_cfg\":{\"testAbc\":\"ab\"},\"silent\":false}", jsonObject.encode());
    }

    @Test
    public void test_toJson_Null() {
        final JsonObject jsonObject = new PreDeploymentResult("1", EventType.REMOVE, State.ENABLED, "serviceId", null,
                                                              null).toJson();
        Assert.assertEquals("{\"transaction_id\":\"1\",\"event\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                            "\"service_id\":\"serviceId\",\"silent\":false}", jsonObject.encode());
    }

    @Test
    public void test_convert_from_json() {
        final JsonObject jsonObject = new PreDeploymentResult("1", EventType.REMOVE, State.ENABLED, "serviceId",
                                                              "deployId",
                                                              Collections.singletonMap("testAbc", "ab")).toJson();
        PreDeploymentResult preResult = PreDeploymentResult.fromJson(jsonObject);
        Assert.assertNotNull(preResult);
        Assert.assertEquals("1", preResult.getTransactionId());
        Assert.assertEquals("serviceId", preResult.getServiceId());
        Assert.assertEquals("deployId", preResult.getDeployId());
        Assert.assertEquals(EventType.REMOVE, preResult.getEvent());
        Assert.assertEquals(State.ENABLED, preResult.getPrevState());
        Assert.assertEquals("{\"testAbc\":\"ab\"}", preResult.getDeployCfg().encode());
    }

}