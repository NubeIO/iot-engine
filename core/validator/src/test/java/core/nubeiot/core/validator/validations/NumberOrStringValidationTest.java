package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.NumberOrStringValidation;

import io.vertx.core.json.JsonObject;

public class NumberOrStringValidationTest {

    @Test
    public void test_numeric_or_string_success() {
        Validation<Object, ?> validation = new NumberOrStringValidation<>();

        validation.validate(1).test().assertValue(v -> true);
        validation.validate(1.0).test().assertValue(v -> true);
        validation.validate("1").test().assertValue(v -> true);
    }

    @Test
    public void test_numeric_or_string_failure() {
        Validation<Object, ?> validation = new NumberOrStringValidation<>();

        validation.validate(new JsonObject().put("a", 1)).test().assertError(e -> {
            System.out.println(e.getMessage());
            return true;
        });
    }

}
