package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.JObject;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class JObjectTest {

    private static final Logger logger = LoggerFactory.getLogger(JObjectTest.class);

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", new JsonObject().put("result", "success"));
        JsonObject jsonObject1 = new JsonObject().put("a", new JsonArray().add(1).add(2).add(3));

        Validation<?, ?> validation = new JObject<>();

        validation.validate(jsonObject, "a").test().assertValue(value -> {
            logger.info(value.getData());
            return true;
        });
        validation.validate(jsonObject1, "a").test().assertError(error -> true);
    }

}
