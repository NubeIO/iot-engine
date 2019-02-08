package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.Required;

public class RequiredTest {

    @Test
    public void test_required_integer_success() {
        Validation<Object> validation = new Required<>(new IntegerValidation<>());
        Assert.assertTrue(validation.validate(1).isValid());
    }

    @Test
    public void test_required_integer_failure() {
        Validation<Object> validation = new Required<>(new IntegerValidation<>());
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate("1").getErrors()).getMessage(),
            "ValidationError: 1 is not the type of java.lang.Integer");
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate(null).getErrors())
                                                              .getMessage(),
            "ValidationError: null required value is null");
    }

}
