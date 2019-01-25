package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.IntegerValidation;

public class IntegerValidationTest {

    @Test
    public void test_integer_success() {
        Validation<Integer, Integer> validation = new IntegerValidation<>();
        validation.validate(1).test().assertValue(value -> value.getData() == 1);
    }

    @Test
    public void test_integer_failure() {
        Validation<Object, ?> validation = new IntegerValidation<>();
        validation.validate(1.1).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
        validation.validate("1").test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
