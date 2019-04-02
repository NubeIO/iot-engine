package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.NumberOrStringValidation;

import io.vertx.core.json.JsonObject;

public class NumberOrStringValidationTest {

    @Test
    public void test_numeric_or_string_success() {
        Validation<Object> validation = new NumberOrStringValidation<>();
        Assert.assertTrue(validation.validate(1).isValid());
        Assert.assertTrue(validation.validate(1.0).isValid());
        Assert.assertTrue(validation.validate("ABC").isValid());
    }

    @Test
    public void test_numeric_or_string_failure() {
        Validation<Object> validation = new NumberOrStringValidation<>();

        System.out.println(ValidationUtils.convertValidationErrorsToException.apply(
            validation.validate(new JsonObject().put("a", 1)).getErrors()).getMessage());
        Assert.assertFalse(validation.validate(new JsonObject().put("a", 1)).isValid());
    }

}
