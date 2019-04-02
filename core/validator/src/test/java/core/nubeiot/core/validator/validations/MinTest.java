package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Min;

public class MinTest {

    @Test
    public void test_min_success() {
        Validation<Number> validation = new Min<>(5d);
        Assert.assertTrue(validation.validate(10.0).isValid());
        Assert.assertTrue(validation.validate(10).isValid());
    }

    @Test
    public void test_min_failure() {
        Validation<Number> validation = new Min<>(5d);
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(4.0).errors()).getMessage(),
            "ValidationError: 4.0 is not greater than or equal to 5.0");
    }

}
