package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.JsonConverter;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;
import com.nubeiot.core.validator.validations.KeyLoop;
import com.nubeiot.core.validator.validations.StringValidation;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class KeyLoopTest {

    @Test
    public void test_json_object_values_as_strings_success() {
        class ObjectValidation<V> implements ValidationSupplier<V> {

            private Validation<?, ?> sv = new StringValidation<>();

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
            }

        }

        JsonObject jsonObject = new JsonObject().put("first_name", "Shane").put("last_name", "Watson");
        Validation<Object, ?> keyLoop = new KeyLoop<>(new ObjectValidation<>());
        keyLoop.validate(jsonObject).test().assertValue(value -> {
            System.out.println(value.getData());
            return true;
        });
    }

    @Test
    public void test_json_object_values_as_strings_failure() {
        class ObjectValidation<V> implements ValidationSupplier<V> {

            private Validation<?, ?> sv = new StringValidation<>();

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
            }

        }

        JsonObject jsonObject = new JsonObject().put("first_name", "Shane").put("last_name", "Watson").put("age", 33);
        Validation<Object, ?> keyLoop = new KeyLoop<>(new ObjectValidation<>());
        keyLoop.validate(jsonObject).test().assertError(e -> {
            System.out.println(e.getMessage());
            return true;
        });
    }

    @Test
    public void test_json_object_values_as_strings_failure_with_json_converter() {
        class ObjectValidation<V> implements ValidationSupplier<V> {

            private Validation<?, ?> sv = new StringValidation<>();

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
            }

        }

        JsonObject jsonObject = new JsonObject().put("details", new JsonObject().put("first_name", "Shane")
                                                                                .put("last_name", "Watson")
                                                                                .put("age", 33));
        Validation<Object, ?> keyLoop = new KeyLoop<>(new ObjectValidation<>());

        JsonConverter.validate(keyLoop, jsonObject, "details").test().assertError(e -> {
            System.out.println(e.getMessage());
            return true;
        });
    }

}
