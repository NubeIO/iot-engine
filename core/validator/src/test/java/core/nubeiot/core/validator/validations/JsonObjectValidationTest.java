package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.JsonObjectValidation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonObjectValidationTest {

    @Test
    public void test_json_object_success() {
        Validation<JsonObject> validation = new JsonObjectValidation<>();

        Assert.assertTrue(validation.validate(new JsonObject().put("result", "success")).isValid());
    }

    @Test
    public void test_json_object_failure() {
        Validation<Object> validation = new JsonObjectValidation<>();

        NubeException nubeException = ValidationUtils.convertValidationErrorsToException.apply(
            validation.validate(new JsonArray().add(1).add(2)).getErrors());

        Assert.assertEquals(nubeException.getMessage(),
                            "ValidationError: [1,2] is not the type of io.vertx.core.json.JsonObject");
    }

}
