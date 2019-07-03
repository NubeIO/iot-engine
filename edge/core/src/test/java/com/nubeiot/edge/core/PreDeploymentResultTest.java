package com.nubeiot.edge.core;

import static com.nubeiot.core.NubeConfig.constructNubeConfig;

import java.util.Collections;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;

public class PreDeploymentResultTest {

    @Test
    public void test_toJson_NonNull() throws JSONException {
        PreDeploymentResult preResult = PreDeploymentResult.builder()
                                                           .transactionId("1")
                                                           .action(EventAction.REMOVE)
                                                           .prevState(State.ENABLED)
                                                           .targetState(State.ENABLED)
                                                           .serviceId("serviceId")
                                                           .serviceFQN("serviceFQN")
                                                           .deployId("deployId")
                                                           .systemConfig(new JsonObject())
                                                           .appConfig(JsonObject.mapFrom(
                                                               Collections.singletonMap("testAbc", "ab")))
                                                           .secretConfig(new JsonObject())
                                                           .build();
        NubeConfig nubeCfg = constructNubeConfig(preResult.getSystemConfig(), preResult.getAppConfig());
        JsonObject preResultJson = preResult.toJson();
        JsonData.removeKeys(preResultJson, "app_config", "system_config", "secret_config");
        JSONAssert.assertEquals(
            "{\"transaction_id\":\"1\",\"action\":\"REMOVE\",\"prev_state\":\"ENABLED\",\"target_state\":\"ENABLED\"," +
            "\"service_id\":\"serviceId\",\"service_fqn\":\"serviceFQN\",\"deploy_id\":\"deployId\"," +
            "\"silent\":false}", preResultJson.encode(), JSONCompareMode.STRICT);
        Assert.assertNotNull(nubeCfg);
        Assert.assertNull(nubeCfg.getSystemConfig());
        Assert.assertNotNull(nubeCfg.getDataDir());
        Assert.assertNotNull(nubeCfg.getAppConfig());
        Assert.assertEquals(1, nubeCfg.getAppConfig().size());
        Assert.assertEquals("ab", nubeCfg.getAppConfig().get("testAbc"));
        Assert.assertNotNull(nubeCfg.getDeployConfig());
    }

    @Test
    public void test_toJson_Null() throws JSONException {
        PreDeploymentResult preResult = PreDeploymentResult.builder()
                                                           .transactionId("1")
                                                           .action(EventAction.REMOVE)
                                                           .prevState(State.ENABLED)
                                                           .targetState(State.ENABLED)
                                                           .serviceId("serviceId")
                                                           .serviceFQN("serviceFQN")
                                                           .systemConfig(new JsonObject())
                                                           .appConfig(new JsonObject())
                                                           .secretConfig(new JsonObject())
                                                           .build();
        NubeConfig nubeCfg = constructNubeConfig(preResult.getSystemConfig(), preResult.getAppConfig());
        JsonObject preResultJson = preResult.toJson();
        JsonData.removeKeys(preResultJson, "app_config", "system_config", "secret_config");
        System.out.println(nubeCfg.toJson());
        JSONAssert.assertEquals(
            "{\"transaction_id\":\"1\",\"action\":\"REMOVE\",\"prev_state\":\"ENABLED\",\"target_state\":\"ENABLED\"," +
            "\"service_id\":\"serviceId\",\"service_fqn\":\"serviceFQN\",\"silent\":false}", preResultJson.encode(),
            JSONCompareMode.STRICT);
        Assert.assertNotNull(nubeCfg);
        Assert.assertNotNull(nubeCfg.getAppConfig());
        Assert.assertTrue(nubeCfg.getAppConfig().isEmpty());
        Assert.assertNotNull(nubeCfg.getDeployConfig());
        Assert.assertNull(nubeCfg.getSystemConfig());
    }

    @Test
    public void test_deserialize() {
        JsonObject jsonObject = PreDeploymentResult.builder()
                                                   .transactionId("1")
                                                   .action(EventAction.REMOVE)
                                                   .prevState(State.ENABLED)
                                                   .targetState(State.ENABLED)
                                                   .serviceId("serviceId")
                                                   .serviceFQN("serviceFQN")
                                                   .deployId("deployId")
                                                   .systemConfig(new JsonObject())
                                                   .appConfig(
                                                       JsonObject.mapFrom(Collections.singletonMap("testAbc", "ab")))
                                                   .secretConfig(new JsonObject())
                                                   .build()
                                                   .toJson();
        PreDeploymentResult preResult = JsonData.from(jsonObject, PreDeploymentResult.class);
        Assert.assertNotNull(preResult);
        Assert.assertEquals("1", preResult.getTransactionId());
        Assert.assertEquals("serviceId", preResult.getServiceId());
        Assert.assertEquals("serviceFQN", preResult.getServiceFQN());
        Assert.assertEquals("deployId", preResult.getDeployId());
        Assert.assertEquals(EventAction.REMOVE, preResult.getAction());
        Assert.assertEquals(State.ENABLED, preResult.getPrevState());
        Assert.assertEquals(State.ENABLED, preResult.getTargetState());
        Assert.assertEquals("{\"testAbc\":\"ab\"}", preResult.getAppConfig().toJson().encode());
    }

}
