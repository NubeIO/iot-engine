package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.JsonConverter;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;
import com.nubeiot.core.validator.validations.Loop;
import com.nubeiot.core.validator.validations.StringValidation;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class LoopTest {

    @Test
    public void test_json_object_values_as_strings_success() {
        class JsonArrayValidation<V> implements ValidationSupplier<V> {

            private Validation<?, ?> sv = new StringValidation<>();

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
            }

        }

        JsonArray jsonArray = new JsonArray().add("Hello").add("World");
        Validation<Object, ?> loop = new Loop<>(new JsonArrayValidation<>());
        loop.validate(jsonArray).test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });
    }

    @Test
    public void test_json_object_values_as_strings_failure() {
        class JsonArrayValidation<V> implements ValidationSupplier<V> {

            private Validation<?, ?> sv = new StringValidation<>();

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
            }

        }

        JsonArray jsonArray = new JsonArray().add("Hello").add(123);
        Validation<Object, ?> loop = new Loop<>(new JsonArrayValidation<>());
        loop.validate(jsonArray).test().assertError(e -> {
            System.out.println(e.getMessage());
            return true;
        });
    }

    @Test
    public void test_json_object_values_as_strings_failure_with_json_converter() {
        class JsonArrayValidation<V> implements ValidationSupplier<V> {

            private Validation<?, ?> sv = new StringValidation<>();

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
            }

        }

        JsonObject jsonObject = new JsonObject().put("details", new JsonArray().add("Hello").add(123));
        Validation<Object, ?> loop = new Loop<>(new JsonArrayValidation<>());

        JsonConverter.validate(loop, jsonObject, "details").test().assertError(e -> {
            System.out.println(e.getMessage());
            return true;
        });
    }

}
