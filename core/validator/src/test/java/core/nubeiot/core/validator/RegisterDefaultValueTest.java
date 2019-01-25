package core.nubeiot.core.validator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nubeiot.core.validator.JsonConverter;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Composition;
import com.nubeiot.core.validator.validations.Max;
import com.nubeiot.core.validator.validations.Min;

import io.vertx.core.json.JsonObject;

public class RegisterDefaultValueTest {

    @Test
    public void test_default_value_insertion_success() {
        JsonObject jsonObject = new JsonObject().put("a", 10);

        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new Min<>(1d));
        validations.add(new Max<>(16d));

        Validation<Object, List<?>> validation = new Composition<>(validations);

        JsonConverter.validate(validation, jsonObject, "", "b", 16).test().assertValue(output -> {
            System.out.println(jsonObject);
            return true;
        });
    }

    @Test
    public void test_without_default_value_success() {
        JsonObject jsonObject = new JsonObject().put("a", 10);

        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new Min<>(1d));
        validations.add(new Max<>(16d));

        Validation<Object, List<?>> validation = new Composition<>(validations);

        JsonConverter.validate(validation, jsonObject, "", "b").test().assertValue(output -> {
            System.out.println(jsonObject);
            return true;
        });
    }

}
