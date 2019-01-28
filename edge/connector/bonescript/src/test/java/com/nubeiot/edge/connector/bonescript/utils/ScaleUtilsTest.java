package com.nubeiot.edge.connector.bonescript.utils;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.FileUtilsTest;

import io.vertx.core.json.JsonObject;

public class ScaleUtilsTest {

    private static final URL ANALOG_OUT_TEMPLATE_RESOURCE = FileUtilsTest.class.getClassLoader()
                                                                               .getResource("ditto/points/properties" +
                                                                                            "/analog_out_template" +
                                                                                            ".json");

    @Test
    public void testAnalogOutput() {
        JsonObject point = new JsonObject(FileUtils.readFileToString(ANALOG_OUT_TEMPLATE_RESOURCE.toString()));
        ScaleUtils.analogOutput(point, 1);
        Assert.assertEquals(point.getValue(VALUE), 2.0); // 1.0 + 1.0 (12.0 + offset)
        ScaleUtils.analogOutput(point, 15);
        Assert.assertEquals(point.getValue(VALUE), 13.0);
    }

}
