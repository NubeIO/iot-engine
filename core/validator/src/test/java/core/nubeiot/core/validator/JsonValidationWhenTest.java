package core.nubeiot.core.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.JsonValidationWhen;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Any;
import com.nubeiot.core.validator.validations.Contains;
import com.nubeiot.core.validator.validations.Forbidden;

import io.vertx.core.json.JsonObject;

public class JsonValidationWhenTest {

    @Test
    public void test_when_initialization_failure() {
        JsonObject jsonObject = new JsonObject().put("id", 1).put("value", "ON");

        JsonValidationWhen<Object> validation = new JsonValidationWhen<>().registerField("value");
        validation.registerIs("id", new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3))))
                  .registerThen(new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))));

        NubeException exception = ValidationUtils.convertValidationErrorsToException.apply(
            validation.validate(jsonObject).getErrors());

        Assert.assertEquals(exception.getMessage(),
                            "ValidationError: JsonValidationWhen validation Initialization Error");
        Assert.assertEquals(exception.getErrorCode(), NubeException.ErrorCode.INITIALIZER_ERROR);
    }

    @Test
    public void test_when_success() {
        /* Scenario 1:
        1. JsonValidationWhen id is in [1, 2, 3] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, Forbidden*/
        JsonObject jsonObject = new JsonObject().put("id", 1).put("value", "ON");

        JsonValidationWhen<Object> validation = new JsonValidationWhen<>().registerField("value");
        validation.registerIs("id", new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3))))
                  .registerThen(new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))))
                  .registerOtherwise(new Forbidden<>());

        System.out.println(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(jsonObject).getErrors())
                                                              .getMessage());
        Assert.assertTrue(validation.validate(jsonObject).isValid());
    }

    @Test
    public void test_when_failure() {
        /* Scenario 2:
        1. JsonValidationWhen id is in [1] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, jsonValidationWhen id is in [2] then it's value should be valid on ["OFF"]
        3. Otherwise, pass any value as valid*/
        JsonObject jsonObject = new JsonObject().put("id", 2).put("value", "ON");

        JsonValidationWhen<Object> when = new JsonValidationWhen<>().registerField("value");

        when.registerIs("id", new Contains<>(new HashSet<>(Collections.singletonList(1))))
            .registerThen(new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))))
            .registerOtherwise(
                new JsonValidationWhen<>().registerIs("id", new Contains<>(new HashSet<>(Collections.singletonList(2))))
                                          .registerThen(new Contains<>(new HashSet<>(Collections.singletonList("OFF"))))
                                          .registerOtherwise(new Any<>()));

        NubeException nubeException = ValidationUtils.convertValidationErrorsToException.apply(
            when.validate(jsonObject).getErrors());
        Assert.assertEquals(nubeException.getMessage(), "ValidationError: value strictly should fall in the [OFF]");
    }

}
