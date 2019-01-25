package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Max;

public class MaxTest {

    @Test
    public void test_max_success() {
        Validation<Object, Double> validation = new Max<>(15d);
        validation.validate(10.00).test().assertValue(value -> value.getData() == 10d);
        validation.validate(10).test().assertValue(value -> value.getData() == 10d);
    }

    @Test
    public void test_max_failure() {
        Validation<Object, ?> validation = new Max<>(15d);
        validation.validate(40d).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
