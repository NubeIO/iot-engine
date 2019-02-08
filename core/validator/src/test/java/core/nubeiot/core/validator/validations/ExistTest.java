package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Exist;

public class ExistTest {

    @Test
    public void test_exist_success() {
        Validation<Object> validation = new Exist<>();
        Assert.assertTrue(validation.validate(1).isValid());
    }

    @Test
    public void test_exist_failure() {
        Validation<Object> validation = new Exist<>();
        Assert.assertFalse(validation.validate(null).isValid());
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(null).errors()).getMessage(),
            "ValidationError: required value is null");
    }

}
