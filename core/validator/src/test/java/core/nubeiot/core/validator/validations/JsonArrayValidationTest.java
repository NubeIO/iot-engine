package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.JsonArrayValidation;

import io.vertx.core.json.JsonArray;

public class JsonArrayValidationTest {

    @Test
    public void test_json_array_success() {
        Validation<Object, ?> validation = new JsonArrayValidation<>();

        validation.validate(new JsonArray().add(1).add(2).add(3)).test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });
    }

    @Test
    public void test_json_array_failure() {
        Validation<Object, ?> validation = new JsonArrayValidation<>();

        validation.validate(1).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
