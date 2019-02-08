package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.JsonArrayValidation;

import io.vertx.core.json.JsonArray;

public class JsonArrayValidationTest {

    @Test
    public void test_json_array_success() {
        Validation<Object> validation = new JsonArrayValidation<>();

        Assert.assertTrue(validation.validate(new JsonArray().add(1).add(2).add(3)).isValid());
    }

    @Test
    public void test_json_array_failure() {
        Validation<Object> validation = new JsonArrayValidation<>();

        NubeException nubeException = ValidationUtils.convertValidationErrorsToException.apply(
            validation.validate(1).getErrors());
        Assert.assertEquals(nubeException.getMessage(),
                            "ValidationError: 1 is not the type of io.vertx.core.json.JsonArray");
    }

}
