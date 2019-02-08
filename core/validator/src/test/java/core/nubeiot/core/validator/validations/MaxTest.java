package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Max;

public class MaxTest {

    @Test
    public void test_max_success() {
        Validation<Object> validation = new Max<>(15d);
        Assert.assertTrue(validation.validate(10.00).isValid());
        Assert.assertTrue(validation.validate(10).isValid());
    }

    @Test
    public void test_max_failure() {

        Validation<Object> validation = new Max<>(15d);
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(40d).errors()).getMessage(),
            "ValidationError: 40.0 is not less than or equal to 15.0");
    }

}
