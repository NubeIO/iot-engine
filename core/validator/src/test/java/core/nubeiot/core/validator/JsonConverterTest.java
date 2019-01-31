package core.nubeiot.core.validator;

import org.junit.Test;

import com.nubeiot.core.validator.JsonConverter;
import com.nubeiot.core.validator.validations.JsonArrayValidation;
import com.nubeiot.core.validator.validations.Min;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonConverterTest {

    @Test
    public void test_json_object_converter_success() {
        JsonObject jsonObject = new JsonObject().put("a", 10);
        JsonConverter.validate(new Min<>(1d), jsonObject, "a").test().assertValue(value -> {
            System.out.println(jsonObject);
            return true;
        });
    }

    @Test
    public void test_json_object_converter_with_parent_field_failure() {
        JsonObject jsonObject = new JsonObject().put("a", 10);
        JsonConverter.validate(new Min<>(100d), jsonObject, "xyz", "a").test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

    @Test
    public void test_json_object_converter_with_default_value_success() {
        JsonObject jsonObject = new JsonObject();
        JsonConverter.validate(new Min<>(5d), jsonObject, "xyz", "a", 10).test().assertValue(value -> {
            System.out.println(jsonObject);
            return jsonObject.getValue("a").equals(10);
        });
    }

    @Test
    public void test_json_object_converter_with_default_value_failure() {
        JsonObject jsonObject = new JsonObject();
        JsonConverter.validate(new Min<>(100d), jsonObject, "xyz", "a", 10).test().assertError(error -> {
            System.out.println(error.getMessage());
            return true;
        });
    }

    @Test
    public void test_json_array_converter_success() {
        JsonArray jsonArray = new JsonArray().add(1).add(2);
        JsonObject jsonObject = new JsonObject().put("a", new JsonArray().add(1).add(2));
        JsonConverter.validate(new JsonArrayValidation<>(), jsonArray, "").test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });

        JsonConverter.validate(new JsonArrayValidation<>(), jsonObject, "a").test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });
    }

    @Test
    public void test_json_array_converter_with_parent_field_failure() {
        JsonConverter.validate(new JsonArrayValidation<>(), new JsonObject().put("a", new JsonObject()), "abc[0]", "a")
                     .test()
                     .assertError(error -> {
                         System.out.println(error.getMessage());
                         return error.getMessage()
                                     .equals(
                                         "ValidationError: abc[0].a is not the type of io.vertx.core.json.JsonArray");
                     });
    }

}
