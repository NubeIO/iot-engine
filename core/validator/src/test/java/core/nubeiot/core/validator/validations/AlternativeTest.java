package core.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Alternative;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.NumberValidation;
import com.nubeiot.core.validator.validations.StringValidation;

public class AlternativeTest {

    @Test
    public void test_string_or_number_success() {
        List<Validation<Object>> validations = new ArrayList<>();
        validations.add(new NumberValidation<>());
        validations.add(new StringValidation<>());

        Validation<Object> validation = new Alternative<>(validations);
        Assert.assertTrue(validation.validate("10").isValid());
        Assert.assertTrue(validation.validate(10d).isValid());
    }

    @Test
    public void test_string_or_number_failure() {
        List<Validation<Object>> validations = new ArrayList<>();
        validations.add(new NumberValidation<>());
        validations.add(new IntegerValidation<>());

        Validation<Object> validation = new Alternative<>(validations);
        System.out.println(
            ValidationUtils.convertValidationErrorsToException.apply(validation.validate("1").getErrors())
                                                              .getMessage());
        Assert.assertFalse(validation.validate("1").isValid());
    }

}
