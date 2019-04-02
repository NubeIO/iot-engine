package core.nubeiot.core.validator.validations;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.validator.JsonValidationHolder;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.Validator;
import com.nubeiot.core.validator.utils.ValidationUtils;
import com.nubeiot.core.validator.validations.IntegerValidation;

import io.vertx.core.json.JsonObject;

public class IntegerValidationTest {

    @Test
    public void test_integer_success() {
        Validation<Object> validation = new IntegerValidation<>();
        Assert.assertTrue(validation.validate(1).errors().isEmpty());

        Validator<Object> validator = Validator.builder().validations(Collections.singletonList(validation)).build();
        Assert.assertTrue(validator.execute(1).isValid());
    }

    @Test
    public void test_integer_failure() {
        JsonObject jsonObject = new JsonObject();
        JsonObject source = jsonObject;
        if (source.getJsonObject("a") == null) {
            source.put("a", new JsonObject());
        }
        source = source.getJsonObject("a");

        source.put("b", 5);
        System.out.println(jsonObject);

        Validation<Object> validation = new IntegerValidation<>();
        Assert.assertEquals(validation.validate(1.1).errors().size(), 1);
        Assert.assertEquals(validation.validate(1.1).errors().get(0).build().execute().getMessage(),
                            "ValidationError: 1.1 is not the type of java.lang.Integer");
    }

    @Test
    public void test_integer_json_holder_validation() {
        Validation<Object> validation = new IntegerValidation<>();
        JsonObject jsonObject = new JsonObject().put("a", 1.1).put("b", 1.2);
        JsonValidationHolder<Object> jsonValidationHolder1 = new JsonValidationHolder<>();
        JsonValidationHolder<Object> jsonValidationHolder2 = new JsonValidationHolder<>();
        jsonValidationHolder1.add("a", validation);
        jsonValidationHolder2.add("b", validation);
        ValidationResult validationResult = Validator.builder()
                                                     .validations(
                                                         Arrays.asList(jsonValidationHolder1, jsonValidationHolder2))
                                                     .build()
                                                     .execute(jsonObject);
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(validationResult.errors()).getMessage(),
            "ValidationError: a is not the type of java.lang.Integer && ValidationError: b is not the" +
            " type of java.lang.Integer");
    }

    @Test
    public void test_integer_json_holder_eager_validation() {
        Validation<Object> validation = new IntegerValidation<>();
        JsonObject jsonObject = new JsonObject().put("a", new JsonObject().put("x", "1.1")).put("b", 1.2);
        JsonValidationHolder<Object> jsonValidationHolder1 = new JsonValidationHolder<>();
        JsonValidationHolder<Object> jsonValidationHolder2 = new JsonValidationHolder<>();
        jsonValidationHolder1.add("a.x", validation);
        jsonValidationHolder2.add("b", validation);
        ValidationResult eagerValidationResult = Validator.builder()
                                                          .validations(Arrays.asList(jsonValidationHolder1,
                                                                                     jsonValidationHolder2))
                                                          .build()
                                                          .eagerExecute(jsonObject);
        Assert.assertEquals(
            ValidationUtils.convertValidationErrorsToException.apply(eagerValidationResult.errors()).getMessage(),
            "ValidationError: a.x is not the type of java.lang.Integer");
    }

}
