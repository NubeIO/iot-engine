package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Int;
import com.nubeiot.core.validator.validations.Required;

import core.nubeiot.core.validator.ValidationTest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class RequiredTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ValidationTest.class);

    @Test
    public void test_validateRequired() {
        JsonObject jsonObject = new JsonObject().put("a", 1);
        Validation<?, ?> validation = new Required<>(new Int<>());
        validation.validate(jsonObject, "a").test().assertValue(v -> true);
        validation.validate(jsonObject, "b").test().assertError(e -> {
            logger.error(e.getMessage());
            return true;
        });
    }

}
