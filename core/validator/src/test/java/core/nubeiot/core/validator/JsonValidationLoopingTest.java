package core.nubeiot.core.validator;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.validator.JsonValidationLoop;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.JsonArrayValidation;
import com.nubeiot.core.validator.validations.StringValidation;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonValidationLoopingTest {

    @Test
    public void test_jsonArrayLoop() {
        URL resource = JsonValidationLoopingTest.class.getClassLoader().getResource("example.json");
        JsonArray jsonArray = new JsonArray(FileUtils.readFileToString(resource.toString()));

        JsonValidationLoop<Object> outerLoop = new JsonValidationLoop<>("");
        outerLoop.add("first_name", new StringValidation<>());
        outerLoop.add("last_name", new StringValidation<>());
        outerLoop.add("siblings", new JsonArrayValidation<>());

        JsonValidationLoop<Object> interestsLoop = new JsonValidationLoop<>("details.basic.interests");
        interestsLoop.add("sports", new JsonArrayValidation<>());

        JsonValidationLoop<Object> musicsLoop = new JsonValidationLoop<>("music");
        musicsLoop.add("classical", new JsonArrayValidation<>());
        musicsLoop.add("rock", new JsonArrayValidation<>());

        interestsLoop.add(musicsLoop); // adding JsonValidationLoop inside JsonValidationLoop

        outerLoop.add(interestsLoop); // adding JsonValidationLoop inside JsonValidationLoop

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
        URL resource = JsonValidationLoopingTest.class.getClassLoader().getResource("loop_and_key_loop_example.json");
        JsonObject jsonObject = new JsonObject(FileUtils.readFileToString(resource.toString()));

        JsonValidationLoop<Object> outerLoop = new JsonValidationLoop<>("");
        outerLoop.add("type", new StringValidation<>());
        outerLoop.add("value", new IntegerValidation<>());
        outerLoop.add("attributes", new JsonArrayValidation<>());

        JsonValidationLoop<Object> attributesLoop = new JsonValidationLoop<>("attributes");
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
