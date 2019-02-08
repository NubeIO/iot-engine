package core.nubeiot.core.validator;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.JsonValidationHolder;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.Validator;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.Composition;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.RegexValidation;
import com.nubeiot.core.validator.validations.StringValidation;

import io.vertx.core.json.JsonObject;

public class JsonValidationHolderTest {

    @Test
    public void test_jsonValidationHolderExecute() {
        JsonObject jsonObject = new JsonObject().put("first_name", "Shane")
                                                .put("last_name", 11)
                                                .put("email", "shane@gmail.com")
                                                .put("details", new JsonObject().put("age", 26.2));

        String emailRegex = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b";

        JsonValidationHolder<Object> jsonValidationHolder = new JsonValidationHolder<>();
        jsonValidationHolder.add("first_name", new StringValidation<>());
        jsonValidationHolder.add("last_name", new StringValidation<>());
        jsonValidationHolder.add("email", new Composition<>(
            Arrays.asList(new StringValidation<>(), new RegexValidation<>(emailRegex))));
        jsonValidationHolder.add("details.age", new IntegerValidation<>());

        ValidationResult validationResult = Validator.builder()
                                                     .validations(Collections.singletonList(jsonValidationHolder))
                                                     .build()
                                                     .execute(jsonObject);
        String expected =
            "ValidationError: last_name is not the type of java.lang.String && ValidationError: details.age is not " +
            "the type of java.lang.Integer";
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validationResult.getErrors()).getMessage(),
            expected);
    }

    @Test
    public void test_jsonValidationHolderEagerExecute() {
        JsonObject jsonObject = new JsonObject().put("first_name", "Shane")
                                                .put("last_name", 11)
                                                .put("email", "shane@gmail.com")
                                                .put("details", new JsonObject().put("age", 26.2));

        String emailRegex = "\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b";

        JsonValidationHolder<Object> jsonValidationHolder = new JsonValidationHolder<>();
        jsonValidationHolder.add("first_name", new StringValidation<>());
        jsonValidationHolder.add("last_name", new StringValidation<>());
        jsonValidationHolder.add("email", new Composition<>(
            Arrays.asList(new StringValidation<>(), new RegexValidation<>(emailRegex))));
        jsonValidationHolder.add("details.age", new IntegerValidation<>());

        ValidationResult validationResult = Validator.builder()
                                                     .validations(Collections.singletonList(jsonValidationHolder))
                                                     .build()
                                                     .eagerExecute(jsonObject);
        String expected =
            "ValidationError: last_name is not the type of java.lang.String && ValidationError: details.age is not " +
            "the type of java.lang.Integer";
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validationResult.getErrors()).getMessage(),
            expected);
    }

}
