package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.StringValidation;

public class StringValidationTest {

    @Test
    public void test_string_success() {
        String message = "Hello World!";
        Validation<Object, ?> validation = new StringValidation<>();
        validation.validate(message).test().assertValue(value -> value.getData().equals(message));
    }

    @Test
    public void test_string_failure() {
        Validation<Object, ?> validation = new StringValidation<>();
        validation.validate(1).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
