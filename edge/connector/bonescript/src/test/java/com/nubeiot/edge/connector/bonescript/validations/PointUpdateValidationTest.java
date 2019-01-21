package com.nubeiot.edge.connector.bonescript.validations;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.edge.connector.bonescript.BBPinMappingInitializer;

import io.vertx.core.json.JsonArray;

public class PointUpdateValidationTest {

    @Before
    public void setUp() {
        new BBPinMappingInitializer("v15");
    }

    @Test
    public void test_validation() {
        URL resource = PointUpdateValidationTest.class.getClassLoader().getResource("validations/point_update.json");
        Object object = new JsonArray(FileUtils.readFileToString(resource.toString()));

        Validation<Object, ?> validation = new PointsUpdateValidation<>();
        validation.validate(object).test().assertValue(v -> true);
    }

}
