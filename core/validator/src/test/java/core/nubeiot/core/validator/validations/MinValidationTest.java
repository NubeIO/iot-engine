package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Min;

import io.vertx.core.json.JsonObject;

public class MinValidationTest {

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", 10.000);
        JsonObject jsonObject1 = new JsonObject().put("a", 10);
        JsonObject jsonObject2 = new JsonObject().put("a", 4d);
        Validation<?, Double> validation = new Min<>(5d);
        validation.validate(jsonObject, "a").test().assertValue(value -> value.getData() == 10d);
        validation.validate(jsonObject1, "a").test().assertValue(value -> value.getData() == 10d);
        validation.validate(jsonObject2, "a").test().assertError(error -> true);
    }

}
