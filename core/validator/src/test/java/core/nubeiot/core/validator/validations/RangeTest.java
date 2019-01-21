package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Range;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class RangeTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(NumericOrStringTest.class);

    @Test
    public void test_rangeValidate() {
        JsonObject jsonObject = new JsonObject().put("a", 10.000);
        JsonObject jsonObject1 = new JsonObject().put("a", 10);
        JsonObject jsonObject2 = new JsonObject().put("a", 4.0);
        JsonObject jsonObject3 = new JsonObject().put("a", "4d");
        Validation<?, Double> validation = new Range<>(5d, 15d);
        validation.validate(jsonObject, "a").test().assertValue(value -> value.getData() == 10d);
        validation.validate(jsonObject1, "a").test().assertValue(value -> value.getData() == 10d);
        validation.validate(jsonObject2, "a").test().assertError(error -> {
            logger.error(error.getMessage());
            return true;
        });
        validation.validate(jsonObject3, "a").test().assertError(error -> {
            logger.error(error.getMessage());
            return true;
        });
    }

}
