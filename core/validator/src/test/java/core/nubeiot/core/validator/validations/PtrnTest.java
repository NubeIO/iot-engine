package core.nubeiot.core.validator.validations;

import org.junit.Test;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.validations.Ptrn;

import io.vertx.core.json.JsonObject;

public class PtrnTest {

    @Test
    public void test_validate() {
        JsonObject object = new JsonObject().put("a", "0123456");
        JsonObject object1 = new JsonObject().put("a", "012ABC");
        Validation<?, ?> validation = new Ptrn("[0-9]+");
        validation.validate(object, "a").test().assertValue(ignore -> true);
        validation.validate(object1, "a").test().assertError(ignore -> true);
    }

}
