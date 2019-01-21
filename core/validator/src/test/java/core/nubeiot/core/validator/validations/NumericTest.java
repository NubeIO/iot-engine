package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Numeric;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class NumericTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(NumericTest.class);

    @Test
    public void test_validate() {
        JsonObject jsonObject = new JsonObject().put("a", 1);
        JsonObject jsonObject2 = new JsonObject().put("a", 1.0);
        JsonObject jsonObject3 = new JsonObject().put("a", "1");
        Validation<?, ?> validation = new Numeric<>();

        validation.validate(jsonObject, "a").test().assertValue(v -> true);
        validation.validate(jsonObject2, "a").test().assertValue(v -> true);
        validation.validate(jsonObject3, "a").test().assertError(e -> {
            logger.error(e.getMessage());
            return true;
        });
    }

}
