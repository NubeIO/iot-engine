package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.StringValidation;

public class StringValidationTest {

    @Test
    public void test_string_success() {
        String message = "Hello World!";
        Validation<Object> validation = new StringValidation<>();
        Assert.assertTrue(validation.validate(message).isValid());
    }

    @Test
    public void test_string_failure() {
        Validation<Object> validation = new StringValidation<>();
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(1).errors()).getMessage(),
            "ValidationError: 1 is not the type of java.lang.String");
    }

}
