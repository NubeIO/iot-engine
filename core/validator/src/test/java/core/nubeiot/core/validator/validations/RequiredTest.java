package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.Required;

public class RequiredTest {

    @Test
    public void test_required_integer_success() {
        Validation<Object, ?> validation = new Required<>(new IntegerValidation<>());
        validation.validate(1).test().assertValue(v -> {
            System.out.println(v.getData());
            return true;
        });
    }

    @Test
    public void test_required_integer_failure() {
        Validation<Object, ?> validation = new Required<>(new IntegerValidation<>());
        validation.validate("1").test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
        validation.validate(null).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
