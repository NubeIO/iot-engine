package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Int;

import io.vertx.core.json.JsonObject;

public class IntTest {

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", 1.1);
        JsonObject jsonObject1 = new JsonObject().put("a", 1);
        JsonObject jsonObject2 = new JsonObject().put("a", "1");
        Validation<?, Integer> validation = new Int<>();
        validation.validate(jsonObject, "a").test().assertError(error -> true);
        validation.validate(jsonObject1, "a").test().assertValue(value -> value.getData() == 1);
        validation.validate(jsonObject2, "a").test().assertError(error -> true);
    }

}
