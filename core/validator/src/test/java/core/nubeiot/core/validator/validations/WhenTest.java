package core.nubeiot.core.validator.validations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import com.nubeiot.core.validator.JsonConverter;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.validations.Any;
import com.nubeiot.core.validator.validations.Contains;
import com.nubeiot.core.validator.validations.Forbidden;
import com.nubeiot.core.validator.validations.When;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class WhenTest {

    @Test
    public void test_when_success() {

        /* Scenario 1:
        1. When id is in [1, 2, 3] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, pass any value as valid*/
        JsonObject jsonObject = new JsonObject().put("id", 1).put("value", "ON");
        new When<>().registerIs(
            JsonConverter.validate(new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3))), jsonObject, "id"))
                    .registerThen(
                        JsonConverter.validate(new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))), jsonObject,
                                               "value"))
                    .registerOtherwise(new Forbidden<>().validate())
                    .validate(jsonObject.getValue("id"))
                    .test()
                    .assertValue(value -> true);
    }

    @Test
    public void test_when_failure() {
        /* Scenario 2:
        1. When id is in [1] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, when id is in [2] then it's value should be valid on ["ON"]
        3. Otherwise, pass any value as valid*/
        JsonObject jsonObject = new JsonObject().put("id", 2).put("value", "ON");

        Single<ValidationResult<Object>> whenId2 = new When<>().registerIs(
            JsonConverter.validate(new Contains<>(new HashSet<>(Collections.singletonList(2))), jsonObject, "id"))
                                                               .registerThen(JsonConverter.validate(new Contains<>(
                                                                                                        new HashSet<>(Collections.singletonList("OFF"))),
                                                                                                    jsonObject,
                                                                                                    "value"))
                                                               .registerOtherwise(new Any<>().validate())
                                                               .validate(jsonObject.getValue("id"));

        Single<ValidationResult<Object>> whenId1 = new When<>().registerIs(
            JsonConverter.validate(new Contains<>(new HashSet<>(Collections.singletonList(1))), jsonObject, "id"))
                                                               .registerThen(JsonConverter.validate(new Contains<>(
                                                                                                        new HashSet<>(Arrays.asList("ON", "OFF"))),
                                                                                                    jsonObject,
                                                                                                    "value"))
                                                               .registerOtherwise(whenId2)
                                                               .validate(jsonObject.getValue("id"));

        whenId1.test()
               .assertError(error -> error.getMessage()
                                          .equals("ValidationError: \"value\" strictly should fall in the" + " [OFF]"));
    }

    @Test
    public void test_skip_when_validation_when_value_null_success() {

        /* Scenario 1:
        1. When id is in [1, 2, 3] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, pass any value as valid*/
        JsonObject jsonObject = new JsonObject().put("id", 10).put("value", "ON");
        new When<>().registerIs(
            JsonConverter.validate(new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3))), jsonObject, "id"))
                    .registerThen(
                        JsonConverter.validate(new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))), jsonObject,
                                               "value"))
                    .registerOtherwise(new Forbidden<>().validate())
                    .validate(null) // value is null
                    .test()
                    .assertValue(value -> true);
    }

    @Test
    public void test_skip_when_validation_when_value_null_with_json_success() {

        /* Scenario 1:
        1. When id is in [1, 2, 3] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, pass any value as valid*/
        JsonObject jsonObject = new JsonObject().put("value", "ON");
        JsonConverter.validate(new When<>().registerIs(
            JsonConverter.validate(new Contains<>(new HashSet<>(Arrays.asList(1, 2, 3))), jsonObject, "id"))
                                           .registerThen(JsonConverter.validate(
                                               new Contains<>(new HashSet<>(Arrays.asList("ON", "OFF"))), jsonObject,
                                               "value"))
                                           .registerOtherwise(new Forbidden<>().validate()), jsonObject,
                               "id") // JsonObject id value is null
                     .test().assertValue(value -> true);
    }

}
