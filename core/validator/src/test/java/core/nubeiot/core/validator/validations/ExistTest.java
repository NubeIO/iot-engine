package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Exist;

public class ExistTest {

    @Test
    public void test_exist_success() {
        Validation<Object, ?> validation = new Exist<>();
        validation.validate(1).test().assertValue(v -> {
            System.out.println(v.getData());
            return true;
        });
    }

    @Test
    public void test_exist_failure() {
        Validation<?, ?> validation = new Exist<>();
        validation.validate(null).test().assertError(e -> {
            System.out.println(e.getMessage());
            return true;
        });
    }

}
