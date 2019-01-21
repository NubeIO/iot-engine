package core.nubeiot.core.validator.validations;

import java.util.Arrays;

import org.junit.Test;

import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.validations.Any;
import com.nubeiot.core.validator.validations.Items;
import com.nubeiot.core.validator.validations.When;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class WhenTest {

    @Test
    public void test_whenValidation() {

        /* Scenario 1:
        1. When id is in [1, 2, 3] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, pass any value as valid*/
        JsonObject jsonObject = new JsonObject().put("id", 1).put("value", "ON");
        new When<>().registerIs(new Items<>(Arrays.asList(1, 2, 3)).validate(jsonObject, "id"))
                    .registerThen(new Items<>(Arrays.asList("ON", "OFF")).validate(jsonObject, "value"))
                    .registerOtherwise(new Any().validate(null))
                    .validate(null)
                    .test()
                    .assertValue(value -> true);

        /* Scenario 2:
        1. When id is in [1] then it's values should be valid on ["ON" or "OFF"]
        2. Otherwise, when id is in [2] then it's value should be valid on ["ON"]
        3. Otherwise, pass any value as valid*/
        JsonObject jsonObject2 = new JsonObject().put("id", 2).put("value", "ON");

        Single<ValidationResult<Object>> whenId2 = new When<>().registerIs(
            new Items<>(Arrays.asList(2)).validate(jsonObject2, "id"))
                                                               .registerThen(new Items<>(Arrays.asList("OFF"))
                                                                                 .validate(jsonObject2, "value"))
                                                               .registerOtherwise(new Any().validate())
                                                               .validate();

        Single<ValidationResult<Object>> whenId1 = new When<>().registerIs(
            new Items<>(Arrays.asList(1)).validate(jsonObject2, "id"))
                                                               .registerThen(new Items<>(
                                                                             Arrays.asList("ON", "OFF")).validate(
                                                                             jsonObject2, "value"))
                                                               .registerOtherwise(whenId2)
                                                               .validate();

        whenId1.test()
               .assertError(error -> error.getMessage()
                                          .equals("ValidationError: \"value\" field value " +
                                                  "does not fall on the items [OFF]"));
    }

}
