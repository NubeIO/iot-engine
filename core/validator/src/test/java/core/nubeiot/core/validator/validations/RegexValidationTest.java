package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.RegexValidation;

public class RegexValidationTest {

    @Test
    public void test_regex_success() {

        Validation<String> validation = new RegexValidation("[0-9]+");
        Assert.assertTrue(validation.validate("0123456").isValid());
    }

    @Test
    public void test_regex_failure() {
        Validation<String> validation = new RegexValidation("[0-9]+");
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate("012ABC").errors())
                                                              .getMessage(),
            "ValidationError: 012ABC does not match with the pattern [0-9]+");
    }

}
