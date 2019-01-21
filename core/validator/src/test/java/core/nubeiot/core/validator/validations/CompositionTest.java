package core.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nubeiot.core.TestBase;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Composition;
import com.nubeiot.core.validator.validations.Dbl;
import com.nubeiot.core.validator.validations.Max;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CompositionTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(CompositionTest.class);

    @Test
    public void test_composition() {
        JsonObject jsonObject = new JsonObject().put("a", 10.00d);
        JsonObject jsonObject1 = new JsonObject().put("a", 10);

        List<Validation<Object, ?>> validations = new ArrayList<>();
        validations.add(new Dbl<>());
        validations.add(new Max<>(10d));

        Validation<?, List<?>> validation = new Composition<>(validations);
        validation.validate(jsonObject, "a").test().assertValue(output -> true);
        validation.validate(jsonObject1, "a").test().assertError(error -> {
            logger.error(error.getMessage());
            return true;
        });
    }

}
