package core.nubeiot.core.validator;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.enums.ValidationState;

public class ValidationResultTest {

    @Test
    public void test_success() {
        String output = "succeeded";
        ValidationResult<String> validationResult = new ValidationResult<String>().success(output);
        Assert.assertEquals(validationResult.getData(), output);
        Assert.assertEquals(validationResult.getValidity(), ValidationState.VALID);
        Assert.assertNull(validationResult.getReason());
    }

    @Test
    public void test_invalid() {
        String reason = "invalid";
        NubeException nubeException = new ValidationResult<String>().invalid(reason);
        Assert.assertEquals(nubeException.getMessage(), reason);
    }

}
