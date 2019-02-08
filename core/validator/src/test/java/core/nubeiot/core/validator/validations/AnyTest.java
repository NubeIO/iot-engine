package core.nubeiot.core.validator.validations;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.validations.Any;

public class AnyTest {

    @Test
    public void test_any_success() {

        Assert.assertTrue(new Any<>().validate().isValid());
        Assert.assertTrue(new Any<>().validate(null).isValid());
        Assert.assertTrue(new Any<>().validate("XYZ").isValid());
    }

}
