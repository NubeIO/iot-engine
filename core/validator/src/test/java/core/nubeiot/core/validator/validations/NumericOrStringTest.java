package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.NumericOrString;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class NumericOrStringTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(NumericOrStringTest.class);

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", 1);
        JsonObject jsonObject2 = new JsonObject().put("a", 1.0);
        JsonObject jsonObject3 = new JsonObject().put("a", "1");
        JsonObject jsonObject4 = new JsonObject().put("a", new JsonObject());
        Validation<?, ?> validation = new NumericOrString<>();

        validation.validate(jsonObject, "a").test().assertValue(v -> true);
        validation.validate(jsonObject2, "a").test().assertValue(v -> true);
        validation.validate(jsonObject3, "a").test().assertValue(v -> true);
        validation.validate(jsonObject4, "a").test().assertError(e -> {
            logger.error(e.getMessage());
            return true;
        });
    }

}
