package core.nubeiot.core.validator;

import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.validator.JsonConverter;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;
import com.nubeiot.core.validator.validations.Composition;
import com.nubeiot.core.validator.validations.Exist;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.JsonArrayValidation;
import com.nubeiot.core.validator.validations.JsonObjectValidation;
import com.nubeiot.core.validator.validations.KeyLoop;
import com.nubeiot.core.validator.validations.Loop;
import com.nubeiot.core.validator.validations.StringValidation;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

public class ValidationTest {

    private JsonObject object = new JsonObject().put("first_name", "Binod")
                                                .put("last_name", "Rai")
                                                .put("details", new JsonObject().put("age", 26.2));

    @Test
    public void test_field_does_not_exist_when_is_required_true() {
        Validation<?, ?> sv = new StringValidation<>();
        Validation<Object, ?> iv = new IntegerValidation<>();
        Validation<Object, ?> ev = new Exist<>();

        JsonConverter.validate(sv, object, "first_name")
                     .flatMap(ignore -> JsonConverter.validate(sv, object, "last_name"))
                     .flatMap(ignore -> JsonConverter.validate(new Composition<>(Arrays.asList(iv, ev)), object,
                                                               "details.xyz"))
                     .test()
                     .assertError(error -> {
                         System.out.println(error.getMessage());
                         return true;
                     });
    }

    @Test
    public void test_no_error_on_non_existing_value_when_is_require_false() {
        Validation<?, ?> sv = new StringValidation<>();
        Validation<?, ?> iv = new IntegerValidation<>();

        JsonConverter.validate(sv, object, "first_name")
                     .flatMap(ignore -> JsonConverter.validate(sv, object, "last_name"))
                     .flatMap(ignore -> JsonConverter.validate(iv, object, "details.xyz"))
                     .test()
                     .assertValue(value -> true);
    }

    @Test
    public void test_integer_validation() {

        Validation<?, ?> sv = new StringValidation<>();
        Validation<?, ?> iv = new IntegerValidation<>();

        JsonConverter.validate(sv, object, "first_nam")
                     .flatMap(ignore -> JsonConverter.validate(sv, object, "last_name"))
                     .flatMap(ignore -> JsonConverter.validate(iv, object, "details.age"))
                     .test()
                     .assertError(error -> {
                         System.out.println(error.getMessage());
                         return true;
                     });
    }

    @Test
    public void test_loop_and_key_loop_failure() {

        URL resource = ValidationTest.class.getClassLoader().getResource("loop_and_key_loop_example.json");
        JsonObject jsonObject = new JsonObject(FileUtils.readFileToString(resource.toString()));

        class DetailsJsonArrayValidation<V> implements ValidationSupplier<V> {

            private Validation<?, ?> detailsJsonObjectValidation = new KeyLoop<>(new DetailsJsonObjectValidation<>());

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                // 2. jsonObject.details[i] value must be of JsonObject, and we loop that JsonObject on its each key
                return JsonConverter.validate(detailsJsonObjectValidation, s, parentField, "").map(x -> x);
            }

            class DetailsJsonObjectValidation<W> implements ValidationSupplier<W> {

                Validation<Object, ?> innerLoop = new Loop<>(new InnerArrayLoopValidation<>());

                @Override
                public Single<ValidationResult<?>> get(W s, String parentField) {
                    // 3. jsonObject.details[i].key_value must be of JsonArray
                    return JsonConverter.validate(innerLoop, s, parentField, "").map(x -> x);
                }

                class InnerArrayLoopValidation<X> implements ValidationSupplier<X> {

                    private Validation<?, ?> sv = new StringValidation<>();

                    @Override
                    public Single<ValidationResult<?>> get(X s, String parentField) {
                        // 4. jsonObject.details[i].key_value[j] must be of String
                        return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
                    }

                }

            }

        }

        // 1. jsonObject.details should be of array type, so we loop here
        Validation<Object, ?> loop = new Loop<>(new DetailsJsonArrayValidation<>());
        JsonConverter.validate(loop, jsonObject, "details").test().assertError(e -> {
            System.out.println(e.getMessage());
            return e.getMessage().equals("ValidationError: details[1].movies[2] is not the type of java.lang.String");
        });
    }

    @Test
    public void test_loop_and_key_loop_failure_2() {
        @NoArgsConstructor
        class FirstLayerObjectValidation<T> implements ValidationSupplier<T> {

            private Validation<?, ?> sv = new StringValidation<>();
            private Validation<?, ?> av = new JsonArrayValidation<>();
            private Validation<?, ?> lvS = new Loop<>(new SiblingValidation<>());
            private Validation<?, ?> lvI = new Loop<>(new InterestValidation<>());

            @Override
            public Single<ValidationResult<?>> get(T s, String parentField) {
                // 2
                return JsonConverter.validate(sv, s, parentField, "first_name")
                                    .flatMap(ignore -> JsonConverter.validate(sv, s, parentField, "last_name"))
                                    .flatMap(ignore -> JsonConverter.validate(av, s, parentField, "siblings",
                                                                              new JsonArray().add("Roshan")
                                                                                             .add("Bhushan")))
                                    .flatMap(siblings -> JsonConverter.validate(lvS, siblings.getData(), parentField,
                                                                                "siblings"))
                                    .flatMap(
                                        ignore -> JsonConverter.validate(av, s, parentField, "details.basic.interests"))
                                    .flatMap(interests -> JsonConverter.validate(lvI, interests.getData(), parentField,
                                                                                 "details.basic.interests"));
            }

            class SiblingValidation<U> implements ValidationSupplier<U> {

                private Validation<U, ?> sv = new StringValidation<>();

                @Override
                public Single<ValidationResult<?>> get(U s, String parentField) {
                    // 3
                    return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
                }

            }


            class InterestValidation<U> implements ValidationSupplier<U> {

                private Validation<?, ?> jov = new JsonObjectValidation<>();

                @Override
                public Single<ValidationResult<?>> get(U s, String parentField) {
                    // 4
                    return JsonConverter.validate(jov, s, parentField, "").flatMap(vrInterest -> {
                        Validation<?, ?> keyLoopInterestValidation = new KeyLoop<>(new InterestObjectValidation<>());
                        JsonObject interest = ((JsonObject) vrInterest.getData());
                        return JsonConverter.validate(keyLoopInterestValidation, interest, parentField, "");
                    });
                }

                class InterestObjectValidation<V> implements ValidationSupplier<V> {

                    private Validation<?, ?> sv = new StringValidation<>();

                    @Override
                    public Single<ValidationResult<?>> get(V s, String parentField) {
                        // 5
                        return JsonConverter.validate(sv, s, parentField, "").map(x -> x);
                    }

                }

            }

        }

        URL resource = ValidationTest.class.getClassLoader().getResource("example.json");
        JsonArray jsonArray = new JsonArray(FileUtils.readFileToString(resource.toString()));

        // 1
        Validation<?, ?> lv = new Loop<>(new FirstLayerObjectValidation<>());
        JsonConverter.validate(lv, jsonArray, "").test().assertError(e -> {
            System.out.println(e.getMessage());
            return e.getMessage()
                    .equals("ValidationError: [1].details.basic.interests[1].music is not the type " +
                            "of java.lang.String");
        });
    }

}

