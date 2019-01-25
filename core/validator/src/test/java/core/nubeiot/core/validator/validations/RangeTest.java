package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Range;

public class RangeTest {

    @Test
    public void test_range_success() {
        Validation<Object, Double> validation = new Range<>(5d, 15d);
        validation.validate(10.0).test().assertValue(value -> value.getData() == 10d);
        validation.validate(10).test().assertValue(value -> value.getData() == 10d);
    }

    @Test
    public void test_range_failure() {
        Validation<Object, ?> validation = new Range<>(5d, 15d);
        validation.validate(4.0).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
        validation.validate("4d").test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
