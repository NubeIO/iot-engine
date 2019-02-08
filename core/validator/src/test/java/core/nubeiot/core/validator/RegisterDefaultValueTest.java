package core.nubeiot.core.validator;

import java.util.Collections;

import org.junit.Test;

import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.core.validator.JsonValidationHolder;
import com.nubeiot.core.validator.Validator;
import com.nubeiot.core.validator.validations.Range;

import io.vertx.core.json.JsonObject;

public class RegisterDefaultValueTest {

    @Test
    public void test_default_value_insertion_success() {
        JsonObject jsonObject = new JsonObject();
        JsonObject defaultJsonObject = new JsonObject().put("a", 18);

        JsonValidationHolder<Object> jsonValidationHolder = new JsonValidationHolder<>();
        jsonValidationHolder.add("a", new Range<>(1d, 17d));
        Validator<Object> validation = Validator.builder()
                                                .validations(Collections.singletonList(jsonValidationHolder))
                                                .mergeFunc(JsonUtils.mergeJsonObjectFunc)
                                                .defaultValue(defaultJsonObject)
                                                .build();
        System.out.println(validation.execute(jsonObject).getErrors());
    }

}
