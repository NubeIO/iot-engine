package com.nubeiot.edge.core;

import java.util.Collections;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;

import io.vertx.core.json.JsonObject;

public class PreDeploymentResultTest {

    @Test
    public void test_toJson_NonNull() throws JSONException {
        PreDeploymentResult preResult = PreDeploymentResult.builder()
                                                           .transactionId("1")
                                                           .action(EventAction.REMOVE)
                                                           .prevState(State.ENABLED)
                                                           .serviceId("serviceId")
                                                           .deployId("deployId")
                                                           .deployCfg(JsonObject.mapFrom(
                                                                   Collections.singletonMap("testAbc", "ab")))
                                                           .build();
        NubeConfig deployCfg = preResult.getDeployCfg();
        JsonObject preResultJson = preResult.toJson();
        preResultJson.remove("deploy_cfg");
        JSONAssert.assertEquals("{\"transaction_id\":\"1\",\"action\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                                "\"service_id\":\"serviceId\",\"deploy_id\":\"deployId\",\"silent\":false}",
                                preResultJson.encode(), JSONCompareMode.STRICT);
        Assert.assertNotNull(deployCfg);
        Assert.assertNull(deployCfg.getSystemConfig());
        Assert.assertNotNull(deployCfg.getDataDir());
        Assert.assertNotNull(deployCfg.getAppConfig());
        Assert.assertEquals(1, deployCfg.getAppConfig().size());
        Assert.assertEquals("ab", deployCfg.getAppConfig().get("testAbc"));
        Assert.assertNotNull(deployCfg.getDeployConfig());
    }

    @Test
    public void test_toJson_Null() throws JSONException {
        PreDeploymentResult preResult = PreDeploymentResult.builder()
                                                           .transactionId("1")
                                                           .action(EventAction.REMOVE)
                                                           .prevState(State.ENABLED)
                                                           .serviceId("serviceId")
                                                           .build();
        JsonObject preResultJson = preResult.toJson();
        preResultJson.remove("deploy_cfg");
        NubeConfig deployCfg = preResult.getDeployCfg();
        System.out.println(deployCfg.toJson());
        JSONAssert.assertEquals("{\"transaction_id\":\"1\",\"action\":\"REMOVE\",\"prev_state\":\"ENABLED\"," +
                                "\"service_id\":\"serviceId\",\"silent\":false}", preResultJson.encode(),
                                JSONCompareMode.STRICT);
        Assert.assertNotNull(deployCfg);
        Assert.assertNotNull(deployCfg.getAppConfig());
        Assert.assertTrue(deployCfg.getAppConfig().isEmpty());
        Assert.assertNotNull(deployCfg.getDeployConfig());
        Assert.assertNull(deployCfg.getSystemConfig());
    }

    @Test
    public void test_deserialize() {
        JsonObject jsonObject = PreDeploymentResult.builder()
                                                   .transactionId("1")
                                                   .action(EventAction.REMOVE)
                                                   .prevState(State.ENABLED)
                                                   .serviceId("serviceId")
                                                   .deployId("deployId")
                                                   .deployCfg(JsonObject.mapFrom(
                                                           Collections.singletonMap("testAbc", "ab")))
                                                   .build()
                                                   .toJson();
        System.out.println(jsonObject);
        PreDeploymentResult preResult = JsonData.from(jsonObject, PreDeploymentResult.class);
        Assert.assertNotNull(preResult);
        Assert.assertEquals("1", preResult.getTransactionId());
        Assert.assertEquals("serviceId", preResult.getServiceId());
        Assert.assertEquals("deployId", preResult.getDeployId());
        Assert.assertEquals(EventAction.REMOVE, preResult.getAction());
        Assert.assertEquals(State.ENABLED, preResult.getPrevState());
        Assert.assertEquals("{\"testAbc\":\"ab\"}", preResult.getDeployCfg().getAppConfig().toJson().encode());
    }

}