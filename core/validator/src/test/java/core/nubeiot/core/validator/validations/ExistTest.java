package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Exist;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ExistTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(NumericOrStringTest.class);

    @Test
    public void test_existValidation() {
        JsonObject jsonObject = new JsonObject().put("a", 1);
        Validation<?, ?> validation = new Exist();
        validation.validate(jsonObject, "a").test().assertValue(v -> true);
        validation.validate(jsonObject, "b").test().assertError(e -> {
            logger.error(e.getMessage());
            return true;
        });
    }

}
