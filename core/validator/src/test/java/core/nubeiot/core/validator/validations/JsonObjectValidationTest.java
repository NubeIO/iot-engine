package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.JsonObjectValidation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonObjectValidationTest {

    @Test
    public void test_json_object_success() {
        Validation<JsonObject, JsonObject> validation = new JsonObjectValidation<>();

        validation.validate(new JsonObject().put("result", "success")).test().assertValue(value -> {
            System.out.println(value.getData());
            return value.getData().getValue("result").equals("success");
        });
    }

    @Test
    public void test_json_object_failure() {
        Validation<Object, Object> validation = new JsonObjectValidation<>();

        validation.validate(new JsonArray().add(1).add(2)).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

}
