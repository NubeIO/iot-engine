package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Dbl;

import io.vertx.core.json.JsonObject;

public class DblTest {

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", 1.000);
        JsonObject jsonObject1 = new JsonObject().put("a", 1);
        JsonObject jsonObject2 = new JsonObject().put("a", 1d);
        Validation<?, Double> validation = new Dbl<>();
        validation.validate(jsonObject, "a").test().assertValue(value -> value.getData() == 1d);
        validation.validate(jsonObject1, "a").test().assertError(error -> true);
        validation.validate(jsonObject2, "a").test().assertValue(value -> value.getData() == 1d);
    }

}
