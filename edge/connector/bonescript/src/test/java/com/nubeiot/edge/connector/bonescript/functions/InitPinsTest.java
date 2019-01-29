package com.nubeiot.edge.connector.bonescript.functions;

import java.util.function.Function;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.TestBase;
import com.nubeiot.edge.connector.bonescript.BoneScriptInit;
import com.nubeiot.edge.connector.bonescript.SingletonBBPinMapping;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;

import io.vertx.core.json.JsonObject;

public class InitPinsTest extends TestBase {

    @Test
    public void testDataInitPins() throws JSONException {
        Function<JsonObject, JsonObject> initPins = new InitPins();
        JSONAssert.assertNotEquals("{}", initPins.apply(BoneScriptInit.initDittoTemplate()).toString(),
                                   JSONCompareMode.STRICT);
        JsonObject jsonObject = initPins.apply(BoneScriptInit.initDittoTemplate());
        int outputSize = jsonObject.getJsonObject("thing")
                                   .getJsonObject("features")
                                   .getJsonObject("points")
                                   .getJsonObject("properties")
                                   .size();
        BBPinMapping bbPinMapping = SingletonBBPinMapping.getInstance();
        Assert.assertEquals(bbPinMapping.getAllPins().size(), outputSize);
    }

}
