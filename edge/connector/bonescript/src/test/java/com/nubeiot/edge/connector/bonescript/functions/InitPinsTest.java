package com.nubeiot.edge.connector.bonescript.functions;

import static com.nubeiot.edge.connector.bonescript.BoneScriptVerticle.BB_DEFAULT_VERSION;

import java.util.function.Function;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.TestBase;
import com.nubeiot.edge.connector.bonescript.BBPinMappingInitializer;
import com.nubeiot.edge.connector.bonescript.Init;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;

import io.vertx.core.json.JsonObject;

public class InitPinsTest extends TestBase {

    @Test
    public void testDataInitPins() throws JSONException {
        new BBPinMappingInitializer(BB_DEFAULT_VERSION);
        Function<JsonObject, JsonObject> initPins = new InitPins();
        JSONAssert.assertNotEquals("{}", initPins.apply(Init.initDittoTemplate()).toString(), JSONCompareMode.STRICT);
        JsonObject jsonObject = initPins.apply(Init.initDittoTemplate());
        int outputSize = jsonObject.getJsonObject("thing")
                                   .getJsonObject("features")
                                   .getJsonObject("points")
                                   .getJsonObject("properties")
                                   .size();
        BBPinMapping bbPinMapping = BBPinMappingInitializer.getInstance();
        Assert.assertEquals(bbPinMapping.getAllPins().size(), outputSize);
    }

}
