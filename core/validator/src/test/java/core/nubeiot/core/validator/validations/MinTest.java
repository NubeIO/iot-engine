package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Min;

public class MinTest {

    @Test
    public void test_min_success() {
        Validation<Number, Double> validation = new Min<>(5d);
        validation.validate(10.00).test().assertValue(value -> value.getData() == 10d);
        validation.validate(10).test().assertValue(value -> value.getData() == 10d);
    }

    @Test
    public void test_min_failure() {
        Validation<Object, ?> validation = new Min<>(5d);
        validation.validate(4d).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
