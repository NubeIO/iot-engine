package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.validations.Forbidden;

public class ForbiddenTest {

    @Test
    public void test_forbidden_success() {
        new Forbidden<>().validate().test().assertError(v -> {
            System.out.println(v.getMessage());
            return v.getMessage().equals("Forbidden: you are not authorized to post this value");
        });

        new Forbidden<>().validate(null).test().assertError(v -> {
            System.out.println(v.getMessage());
            return v.getMessage().equals("Forbidden: you are not authorized to post this value");
        });

        new Forbidden<>().validate("XYZ").test().assertError(v -> {
            System.out.println(v.getMessage());
            return v.getMessage().equals("Forbidden: you are not authorized to post this value");
        });
    }

}
