package core.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Alternative;
import com.nubeiot.core.validator.validations.NumberValidation;
import com.nubeiot.core.validator.validations.StringValidation;

import io.vertx.core.json.JsonObject;

public class AlternativeTest {

    @Test
    public void test_string_or_number_success() {
        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new NumberValidation<>());
        validations.add(new StringValidation<>());

        Validation<Object, ?> validation = new Alternative<>(validations);
        validation.validate("10").test().assertValue(value -> value.getData().equals("10"));
        validation.validate(10d).test().assertValue(value -> value.getData().equals(10d));
    }

    @Test
    public void test_string_or_number_failure() {
        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new NumberValidation<>());
        validations.add(new StringValidation<>());

        Validation<Object, ?> validation = new Alternative<>(validations);
        validation.validate(new JsonObject()).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
