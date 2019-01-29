package com.nubeiot.edge.connector.bonescript.functions;

import java.util.function.Function;

import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bonescript.SingletonBBPinMapping;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class InitPins implements Function<JsonObject, JsonObject> {

    private static final Logger logger = LoggerFactory.getLogger(InitPins.class);
    private static final String ANALOG_IN_TEMPLATE_RESOURCE_PATH = "ditto/points/analog_in_template.json";
    private static final String DIGITAL_IN_TEMPLATE_RESOURCE_PATH = "ditto/points/digital_in_template.json";
    private static final String ANALOG_OUT_TEMPLATE_RESOURCE_PATH = "ditto/points/analog_out_template.json";
    private static final String DIGITAL_OUT_TEMPLATE_RESOURCE_PATH = "ditto/points/digital_out_template.json";

    @Override
    public JsonObject apply(JsonObject jsonObject) {
        BBPinMapping bbPinMapping = SingletonBBPinMapping.getInstance();
        logger.info("BeagleBone version: {}", SingletonBBPinMapping.getVersion());
        String analogInTemplate = Strings.convertToString(
            Reflections.staticClassLoader().getResourceAsStream(ANALOG_IN_TEMPLATE_RESOURCE_PATH));
        String digitalInTemplate = Strings.convertToString(
            Reflections.staticClassLoader().getResourceAsStream(DIGITAL_IN_TEMPLATE_RESOURCE_PATH));
        String analogOutTemplate = Strings.convertToString(
            Reflections.staticClassLoader().getResourceAsStream(ANALOG_OUT_TEMPLATE_RESOURCE_PATH));
        String digitalOutTemplate = Strings.convertToString(
            Reflections.staticClassLoader().getResourceAsStream(DIGITAL_OUT_TEMPLATE_RESOURCE_PATH));

        bbPinMapping.getAllPins()
                    .forEach(pin -> jsonObject.getJsonObject("thing")
                                              .getJsonObject("features")
                                              .getJsonObject("histories")
                                              .getJsonObject("properties")
                                              .put(pin,
                                                   new JsonObject().put("name", pin).put("data", new JsonArray())));

        bbPinMapping.getAnalogInPins()
                    .forEach(pin -> jsonObject.getJsonObject("thing")
                                              .getJsonObject("features")
                                              .getJsonObject("points")
                                              .getJsonObject("properties")
                                              .put(pin, new JsonObject(analogInTemplate.replaceAll("<pin>", pin))));

        bbPinMapping.getDigitalInPins()
                    .forEach(pin -> jsonObject.getJsonObject("thing")
                                              .getJsonObject("features")
                                              .getJsonObject("points")
                                              .getJsonObject("properties")
                                              .put(pin, new JsonObject(digitalInTemplate.replaceAll("<pin>", pin))));

        bbPinMapping.getAnalogOutPins()
                    .forEach(pin -> jsonObject.getJsonObject("thing")
                                              .getJsonObject("features")
                                              .getJsonObject("points")
                                              .getJsonObject("properties")
                                              .put(pin, new JsonObject(analogOutTemplate.replaceAll("<pin>", pin))));

        bbPinMapping.getDigitalOutPins()
                    .forEach(pin -> jsonObject.getJsonObject("thing")
                                              .getJsonObject("features")
                                              .getJsonObject("points")
                                              .getJsonObject("properties")
                                              .put(pin, new JsonObject(digitalOutTemplate.replaceAll("<pin>", pin))));

        return jsonObject;
    }

}
