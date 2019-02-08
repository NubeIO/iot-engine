package core.nubeiot.core.validator;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.validator.JsonLoop;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.JsonArrayValidation;
import com.nubeiot.core.validator.validations.StringValidation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonLoopingTest {

    @Test
    public void test_jsonArrayLoop() {
        URL resource = JsonLoopingTest.class.getClassLoader().getResource("example.json");
        JsonArray jsonArray = new JsonArray(FileUtils.readFileToString(resource.toString()));

        JsonLoop<Object> outerLoop = new JsonLoop<>("");
        outerLoop.add("first_name", new StringValidation<>());
        outerLoop.add("last_name", new StringValidation<>());
        outerLoop.add("siblings", new JsonArrayValidation<>());

        JsonLoop<Object> interestsLoop = new JsonLoop<>("details.basic.interests");
        interestsLoop.add("sports", new JsonArrayValidation<>());

        JsonLoop<Object> musicsLoop = new JsonLoop<>("music");
        musicsLoop.add("classical", new JsonArrayValidation<>());
        musicsLoop.add("rock", new JsonArrayValidation<>());

        interestsLoop.add(musicsLoop); // adding JsonLoop inside JsonLoop

        outerLoop.add(interestsLoop); // adding JsonLoop inside JsonLoop

        ValidationResult validationResult = outerLoop.validate(jsonArray);
        System.out.println(validationResult.getErrors());
        NubeException nubeException = ValidationUtils.convertValidationErrorsToException.apply(
            validationResult.getErrors());
        String expected =
            "ValidationError: [1].details.basic.interests.[0].music.[1].rock is not the type of io.vertx.core.json" +
            ".JsonArray && ValidationError: [1].details.basic.interests.[1].sports is not the type of io.vertx.core" +
            ".json.JsonArray";
        Assert.assertEquals(expected, nubeException.getMessage());
    }

    @Test
    public void test_jsonArrayAndJsonObject() {
        URL resource = JsonLoopingTest.class.getClassLoader().getResource("loop_and_key_loop_example.json");
        JsonObject jsonObject = new JsonObject(FileUtils.readFileToString(resource.toString()));

        JsonLoop<Object> outerLoop = new JsonLoop<>("");
        outerLoop.add("type", new StringValidation<>());
        outerLoop.add("value", new IntegerValidation<>());
        outerLoop.add("attributes", new JsonArrayValidation<>());

        JsonLoop<Object> attributesLoop = new JsonLoop<>("attributes");
        attributesLoop.add("", new StringValidation<>());

        outerLoop.add(attributesLoop);

        ValidationResult validationResult = outerLoop.validate(jsonObject);
        System.out.println(validationResult.getErrors());
        NubeException nubeException = ValidationUtils.convertValidationErrorsToException.apply(
            validationResult.getErrors());
        String expected =
            "ValidationError: UI2.value is not the type of java.lang.Integer && ValidationError: UI2.attributes.[0]" +
            " is not the type of java.lang.String";
        Assert.assertEquals(expected, nubeException.getMessage());
    }

}
