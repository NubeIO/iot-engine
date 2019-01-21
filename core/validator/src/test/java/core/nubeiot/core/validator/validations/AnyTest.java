package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.validations.Any;

public class AnyTest {

    @Test
    public void test_anyValidation() {
        new Any().validate(null).test().assertValue(v -> true);
        new Any().validate("XYZ").test().assertValue(v -> true);
    }

}
