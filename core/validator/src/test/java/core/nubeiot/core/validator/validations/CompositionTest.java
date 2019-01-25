package core.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Composition;
import com.nubeiot.core.validator.validations.Max;
import com.nubeiot.core.validator.validations.Min;

public class CompositionTest {

    @Test
    public void test_number_with_min_max_success() {
        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new Min<>(1d));
        validations.add(new Max<>(16d));

        Validation<Object, ?> validation = new Composition<>(validations);

        validation.validate(10).test().assertValue(output -> {
            System.out.println(output.getData());
            return true;
        });
    }

    @Test
    public void test_number_with_min_max_failure() {
        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new Min<>(1d));
        validations.add(new Max<>(16d));

        Validation<Object, ?> validation = new Composition<>(validations);

        validation.validate("10").test().assertError(output -> {
            System.out.println(output.toString());
            return true;
        });
    }

}
