package core.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Alternative;
import com.nubeiot.core.validator.validations.Dbl;
import com.nubeiot.core.validator.validations.Str;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AlternativeTest {

    private static final Logger logger = LoggerFactory.getLogger(AlternativeTest.class);

    @Test
    public void test_alternative() {
        JsonObject jsonObject = new JsonObject().put("a", "10");
        JsonObject jsonObject1 = new JsonObject().put("a", 10d);
        JsonObject jsonObject2 = new JsonObject().put("a", 10);

        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new Dbl<>());
        validations.add(new Str<>());

        Validation<?, ?> validation = new Alternative<>(validations);
        validation.validate(jsonObject, "a").test().assertValue(value -> value.getData().equals("10"));
        validation.validate(jsonObject1, "a").test().assertValue(value -> value.getData().equals(10d));
        validation.validate(jsonObject2, "a").test().assertError(error -> {
            logger.error(error.getMessage());
            return true;
        });
    }

}
