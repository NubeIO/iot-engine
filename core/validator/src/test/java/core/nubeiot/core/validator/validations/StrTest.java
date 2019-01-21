package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Str;

import io.vertx.core.json.JsonObject;

public class StrTest {

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", 1);
        JsonObject jsonObject1 = new JsonObject().put("a", "1");
        Validation<?, ?> validation = new Str<>();
        validation.validate(jsonObject, "a").test().assertError(error -> true);
        validation.validate(jsonObject1, "a").test().assertValue(value -> value.getData().equals("1"));
    }

}
