package core.nubeiot.core.validator.validations;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Contains;

import io.vertx.core.json.JsonArray;

public class ContainsTest {

    @Test
    public void test_contains_success() {
        Validation<Object, ?> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)), false);

        validation.validate(new JsonArray().add(1).add(2)).test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });

        validation.validate(1).test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });
    }

    @Test
    public void test_contains_failure() {
        Validation<Object, ?> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        validation.validate(Arrays.asList(1, 6)).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

    @Test
    public void test_contains_when_strict_false_success() {
        Validation<Object, ?> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)), false);

        validation.validate(new JsonArray().add(1).add(7)).test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });
    }

    @Test
    public void test_contains_when_strict_false_failure() {
        Validation<Object, ?> validation = new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)), false);

        validation.validate(new JsonArray().add(7).add(8)).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
