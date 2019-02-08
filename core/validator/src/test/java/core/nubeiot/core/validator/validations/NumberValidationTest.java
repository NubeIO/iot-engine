package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.NumberValidation;

public class NumberValidationTest {

    @Test
    public void test_numeric_success() {
        Validation<Number> validation = new NumberValidation<>();
        Assert.assertTrue(validation.validate(1).isValid());
        Assert.assertTrue(validation.validate(1.0).isValid());
    }

    @Test
    public void test_numeric_failure() {
        Validation<Object> validation = new NumberValidation<>();
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate("1").errors()).getMessage(),
            "ValidationError: 1 is not the type of java.lang.Number");
    }

}
