package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.validations.Forbidden;

public class ForbiddenTest {

    @Test
    public void test_anyValidation() {
        new Forbidden<>().validate(null).test().assertError(v -> v.getMessage().equals(Forbidden.FORBIDDEN_MESSAGE));
        new Forbidden<>().validate("XYZ").test().assertError(v -> v.getMessage().equals(Forbidden.FORBIDDEN_MESSAGE));
    }

}
