package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.JArray;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class JArrayTest {

    private static final Logger logger = LoggerFactory.getLogger(JArrayTest.class);

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", 1);
        JsonObject jsonObject2 = new JsonObject().put("a", new JsonArray().add(1).add(2).add(3));
        JsonArray jsonArray = new JsonArray().add(1).add(2).add(3);
        Validation<JsonArray, ?> validation = new JArray<>();

        validation.validate(jsonObject, "a").test().assertError(error -> true);
        validation.validate(jsonObject2, "a").test().assertValue(value -> {
            logger.info(value.getData());
            return true;
        });
        validation.validate(jsonArray).test().assertValue(value -> {
            logger.info(value.getData());
            return true;
        });
    }

}
