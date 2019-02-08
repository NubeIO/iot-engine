package core.nubeiot.core.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.When;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Any;
import com.nubeiot.core.validator.validations.Contains;
import com.nubeiot.core.validator.validations.Forbidden;

import io.vertx.core.json.JsonObject;

public class WhenTest {

    @Test
    public void test_when_initialization_failure() {
        JsonObject jsonObject = new JsonObject().put("id", 1).put("value", "ON");

        When<Object> validation = new When<>();
        validation.setIs("id", new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3))));
        validation.setThen("value", new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))));
        NubeException exception = ValidationUtils.convertValidationErrorsToException.apply(
            validation.validate(jsonObject).getErrors());

        Assert.assertEquals(exception.getMessage(), "ValidationError: When validation Initialization Error");
        Assert.assertEquals(exception.getErrorCode(), NubeException.ErrorCode.INITIALIZER_ERROR);
    }

    @Test
    public void test_when_success() {
        /* Scenario 1:
        1. When id is in [1, 2, 3] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, Forbidden*/
        JsonObject jsonObject = new JsonObject().put("id", 1).put("value", "ON");

        When<Object> validation = new When<>();
        validation.setIs("id", new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3))));
        validation.setThen("value", new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))));
        validation.setOtherwise(new Forbidden<>());

        System.out.println(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(jsonObject).getErrors())
                                                              .getMessage());
        Assert.assertTrue(validation.validate(jsonObject).isValid());
    }

    @Test
    public void test_when_failure() {
        /* Scenario 2:
        1. When id is in [1] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, when id is in [2] then it's value should be valid on ["OFF"]
        3. Otherwise, pass any value as valid*/
        JsonObject jsonObject = new JsonObject().put("id", 2).put("value", "ON");

        When<Object> whenId1 = new When<>();
        whenId1.setIs("id", new Contains<>(new HashSet<>(Collections.singletonList(1))));
        whenId1.setThen("value", new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))));

        When<Object> whenId2 = new When<>();
        whenId2.setIs("id", new Contains<>(new HashSet<>(Collections.singletonList(2))));
        whenId2.setThen("value", new Contains<>(new HashSet<>(Collections.singletonList("OFF"))));
        whenId2.setOtherwise(new Any<>());

        whenId1.setOtherwise(whenId2);

        NubeException nubeException = ValidationUtils.convertValidationErrorsToException.apply(
            whenId1.validate(jsonObject).getErrors());
        Assert.assertEquals(nubeException.getMessage(), "ValidationError: value strictly should fall in the [OFF]");
    }

}
