package com.nubeiot.edge.core;

import java.util.Collections;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;

import io.vertx.core.json.JsonObject;

public class PreDeploymentResultTest {

    @Test
    public void test_toJson_NonNull() throws JSONException {
        JsonObject jsonObject = new PreDeploymentResult("1", EventAction.REMOVE, State.ENABLED, "serviceId", "deployId",
                                                        Collections.singletonMap("testAbc", "ab")).toJson();
        JSONAssert.assertEquals("{\"transaction_id\":\"1\",\"action\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                                "\"service_id\":\"serviceId\",\"deploy_id\":\"deployId\"," +
                                "\"deploy_cfg\":{\"testAbc\":\"ab\"},\"silent\":false}", jsonObject.encode(),
                                JSONCompareMode.STRICT);
    }

    @Test
    public void test_toJson_Null() throws JSONException {
        JsonObject jsonObject = new PreDeploymentResult("1", EventAction.REMOVE, State.ENABLED, "serviceId", null, null)
                                        .toJson();
        JSONAssert.assertEquals("{\"transaction_id\":\"1\",\"action\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                                "\"service_id\":\"serviceId\",\"silent\":false, \"deploy_cfg\":{}}",
                                jsonObject.encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_convert_from_json() {
        JsonObject jsonObject = new PreDeploymentResult("1", EventAction.REMOVE, State.ENABLED, "serviceId", "deployId",
                                                        Collections.singletonMap("testAbc", "ab")).toJson();
        PreDeploymentResult preResult = PreDeploymentResult.from(jsonObject);
        Assert.assertNotNull(preResult);
        Assert.assertEquals("1", preResult.getTransactionId());
        Assert.assertEquals("serviceId", preResult.getServiceId());
        Assert.assertEquals("deployId", preResult.getDeployId());
        Assert.assertEquals(EventAction.REMOVE, preResult.getAction());
        Assert.assertEquals(State.ENABLED, preResult.getPrevState());
        Assert.assertEquals("{\"testAbc\":\"ab\"}", preResult.getDeployCfg().encode());
    }

}