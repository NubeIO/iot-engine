package com.nubeiot.edge.connector.bonescript.validations;

import java.net.URL;

import org.junit.Test;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.validator.Validation;

import io.vertx.core.json.JsonObject;

public class PointUpdateValidationTest {

    public static final URL VALIDATED_POINT_UPDATE = PointUpdateValidationTest.class.getClassLoader()
                                                                                    .getResource(
                                                                                        "validations/point_update" +
                                                                                        ".json");

    @Test
    public void test_validation() {
        Object object = new JsonObject(FileUtils.readFileToString(VALIDATED_POINT_UPDATE.toString()));

        Validation<Object, ?> validation = new PointsUpdateValidation<>();
        validation.validate(object).test().assertValue(v -> true);
    }

}
