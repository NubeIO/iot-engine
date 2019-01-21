package core.nubeiot.core.validator.validations;

import java.util.Arrays;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Items;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ItemsTest {

    private static final Logger logger = LoggerFactory.getLogger(ItemsTest.class);

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", Arrays.asList(1, 6));
        JsonObject jsonObject1 = new JsonObject().put("a", new JsonArray().add(1).add(2));
        JsonObject jsonObject2 = new JsonObject().put("a", 1);
        Validation<?, ?> validation = new Items<>(Arrays.asList(1, 2, 3, 4, 5));
        validation.validate(jsonObject, "a").test().assertError(error -> {
            logger.error(error.getMessage());
            return true;
        });

        validation.validate(jsonObject1, "a").test().assertValue(value -> {
            logger.info(value.getData());
            return true;
        });

        validation.validate(jsonObject2, "a").test().assertValue(value -> {
            logger.info(value.getData());
            return true;
        });
    }

}
