package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Range;

public class RangeTest {

    @Test
    public void test_range_success() {
        Validation<Object> validation = new Range<>(5d, 15d);
        Assert.assertTrue(validation.validate(10.0).isValid());
        Assert.assertTrue(validation.validate(10).isValid());
    }

    @Test
    public void test_range_failure() {
        Validation<Object> validation = new Range<>(5d, 15d);
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(4.0).errors()).getMessage(),
            "ValidationError: 4.0 should be on range [5.0<= x < 15.0]");
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate("4d").errors()).getMessage(),
            "ClassCastException: 4d is not parsable to Number");
    }

}
