package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.RegexValidation;

public class RegexValidationTest {

    @Test
    public void test_regex_success() {
        Validation<String, ?> validation = new RegexValidation("[0-9]+");
        validation.validate("0123456").test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });
    }

    @Test
    public void test_regex_failure() {
        Validation<String, ?> validation = new RegexValidation("[0-9]+");
        validation.validate("012ABC").test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
