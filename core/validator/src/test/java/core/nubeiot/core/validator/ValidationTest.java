package core.nubeiot.core.validator;

import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;
import com.nubeiot.core.validator.validations.Composition;
import com.nubeiot.core.validator.validations.Exist;
import com.nubeiot.core.validator.validations.Int;
import com.nubeiot.core.validator.validations.JArray;
import com.nubeiot.core.validator.validations.JObject;
import com.nubeiot.core.validator.validations.KeyLoop;
import com.nubeiot.core.validator.validations.Loop;
import com.nubeiot.core.validator.validations.Str;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.NoArgsConstructor;

public class ValidationTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ValidationTest.class);

    private JsonObject object = new JsonObject().put("first_name", "Binod")
                                                .put("last_name", "Rai")
                                                .put("details", new JsonObject().put("age", 26.2));

    @Test
    public void test_fieldDoesNotExistWhenIsRequiredTrue() {
        Validation<?, ?> sv = new Str<>();
        Validation<Object, ?> iv = new Int<>();
        Validation<Object, ?> ev = new Exist<>();

        sv.validate(object, "first_name")
          .flatMap(ignore -> sv.validate(object, "last_name"))
          .flatMap(ignore -> new Composition<>(Arrays.asList(iv, ev)).validate(object, "details.xyz"))
          .test()
          .assertError(error -> {
              logger.error(error.getMessage());
              return true;
          });
    }

    @Test
    public void test_noErrorOnNonExistingValueWhenIsRequireFalse() {
        Validation<?, ?> sv = new Str<>();
        Validation<?, ?> iv = new Int<>();

        sv.validate(object, "first_name")
          .flatMap(ignore -> sv.validate(object, "last_name"))
          .flatMap(ignore -> iv.validate(object, "details.xyz"))
          .test()
          .assertValue(value -> true);
    }

    @Test
    public void test_integerValidation() {

        Validation<?, ?> sv = new Str<>();
        Validation<?, ?> iv = new Int<>();

        sv.validate(object, "first_nam")
          .flatMap(ignore -> sv.validate(object, "last_name"))
          .flatMap(ignore -> iv.validate(object, "details.age"))
          .test()
          .assertError(error -> {
              logger.error(error.getMessage());
              return true;
          });
    }

    @Test
    public void test_arrayValidationWithSupplier() {
        @NoArgsConstructor
        class FirstLayerObjectValidation<T> implements ValidationSupplier<T> {

            private Validation<?, ?> sv = new Str<>();
            private Validation<?, ?> av = new JArray<>();
            private Loop lvS = new Loop(new SiblingValidation<>(), "siblings");
            private Loop lvI = new Loop(new InterestValidation<>(), "details.basic.interests");

            @Override
            public Single<ValidationResult<?>> get(T s, String parentField) {
                return sv.validate(s, parentField, "first_name")
                         .flatMap(ignore -> sv.validate(s, parentField, "last_name"))
                         .flatMap(ignore -> av.registerDefaultValue(Arrays.asList("Binod", "Roshan", "Bhushan"))
                                              .validate(s, parentField, "siblings"))
                         .flatMap(siblings -> lvS.validate(siblings.getData(), parentField, "siblings"))
                         .flatMap(ignore -> av.validate(s, parentField, "details.basic.interests"))
                         .flatMap(interests -> lvI.validate(interests.getData(), parentField, ""));
            }

            class SiblingValidation<U> implements ValidationSupplier<U> {

                private Validation<U, ?> sv = new Str<>();

                @Override
                public Single<ValidationResult<?>> get(U s, String parentField) {
                    return sv.registerParentField(parentField).validate(s).map(x -> x);
                }

            }


            class InterestValidation<U> implements ValidationSupplier<U> {

                private Validation<?, ?> jov = new JObject<>();

                @Override
                public Single<ValidationResult<?>> get(U s, String parentField) {
                    return jov.validate(s, parentField, "").flatMap(vrInterest -> {
                        Validation<JsonObject, ?> lvI = new KeyLoop(new InterestObjectValidation<>(), parentField);
                        JsonObject interest = ((JsonObject) vrInterest.getData());
                        return lvI.registerParentField(parentField).validate(interest);
                    });
                }

                class InterestObjectValidation<V> implements ValidationSupplier<V> {

                    private Validation<?, ?> sv = new Str<>();

                    @Override
                    public Single<ValidationResult<?>> get(V s, String parentField) {
                        return sv.validate(s, parentField, "").map(x -> x);
                    }

                }

            }

        }

        URL resource = ValidationTest.class.getClassLoader().getResource("example.json");
        Object object = new JsonArray(FileUtils.readFileToString(resource.toString()));

        Validation<Object, ?> av = new JArray<>();
        Validation<JsonArray, ?> lv = new Loop(new FirstLayerObjectValidation<>(), "");

        av.validate(object)
          .flatMap(vrArray -> {
              JsonArray array = (JsonArray) vrArray.getData();
              return lv.validate(array);
          }).test().assertError(error -> {
            logger.error(error.getMessage());
            return true;
        });
    }

}

