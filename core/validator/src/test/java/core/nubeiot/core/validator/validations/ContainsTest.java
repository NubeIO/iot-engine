package core.nubeiot.core.validator.validations;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Contains;

import io.vertx.core.json.JsonArray;

public class ContainsTest {

    @Test
    public void test_contains_success() {
        Validation<Object> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)), false);

        Assert.assertTrue(validation.validate(new JsonArray().add(1).add(2)).isValid());
        Assert.assertTrue(validation.validate(1).isValid());
    }

    @Test
    public void test_contains_failure() {
        Validation<Object> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        Assert.assertEquals(ValidationUtils.convertValidationErrorsToException.apply(
            validation.validate(new JsonArray().add(1).add(8)).getErrors()).getMessage(),
                            "ValidationError: [1,8] strictly should fall in the [1, 2, 3, 4, 5]");
    }

    @Test
    public void test_contains_when_strict_false_success() {
        Validation<Object> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)), false);
        Assert.assertTrue(validation.validate(new JsonArray().add(1).add(8)).isValid());
    }

    @Test
    public void test_contains_when_strict_false_failure() {
        Validation<Object> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)), false);

        Assert.assertEquals(ValidationUtils.convertValidationErrorsToException.apply(
            validation.validate(new JsonArray().add(7).add(8)).getErrors()).getMessage(),
                            "ValidationError: [7,8] should fall in the [1, 2, 3, 4, 5]");
    }

}
