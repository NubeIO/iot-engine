package core.nubeiot.core.validator;

import org.junit.Test;

import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class ValidationResultTest {

    @Test
    public void test_valid() {
        String output = "succeeded";
        Single<ValidationResult<String>> validationResult = ValidationResult.valid(output);
        validationResult.test().assertValue(value -> value.getData().equals(output));
    }

    @Test
    public void test_invalid() {
        String reason = "invalid";
        Single<ValidationResult<String>> validationResult = ValidationResult.invalid(reason);
        validationResult.test().assertError(value -> value.getMessage().equals("invalid"));
    }

}
