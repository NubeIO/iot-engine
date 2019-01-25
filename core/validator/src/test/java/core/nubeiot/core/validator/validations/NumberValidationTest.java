package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.NumberValidation;

public class NumberValidationTest {

    @Test
    public void test_numeric_success() {
        Validation<Number, ?> validation = new NumberValidation<>();

        validation.validate(1).test().assertValue(v -> true);
        validation.validate(1.0).test().assertValue(v -> true);
    }

    @Test
    public void test_numeric_failure() {
        Validation<Object, ?> validation = new NumberValidation<>();

        validation.validate("1").test().assertError(e -> {
            System.out.println(e.getMessage());
            return true;
        });
    }

}
