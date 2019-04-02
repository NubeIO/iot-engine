package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Forbidden;

public class ForbiddenTest {

    @Test
    public void test_forbidden_success() {
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(new Forbidden<>().validate().errors())
                                                              .getMessage(),
            "Forbidden: you are not authorized to post this value");
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(new Forbidden<>().validate("XYZ").errors())
                                                              .getMessage(),
            "Forbidden: you are not authorized to post this value");
    }

}
