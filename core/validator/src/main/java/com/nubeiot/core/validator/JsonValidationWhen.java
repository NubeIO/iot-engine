package com.nubeiot.core.validator;

import java.util.HashSet;
import java.util.Set;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.utils.ValidationUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JsonValidationWhen<T> implements JsonValidation, Validation<T> {

    private String isField;

    private Validation<Object> is;
    private Validation<Object> then;
    private Validation<Object> otherwise;

    private Set<Integer> inputs = new HashSet<>();

    private String field;

    @Override
    public ValidationResult validity(T s) {
        return this.validate(this, s);
    }

    protected ValidationResult validate(JsonValidationWhen<T> jsonValidationWhen, T s) {
        if (jsonValidationWhen.inputs.size() == 3 &&
            Strings.isNotBlank(field)) { // jsonValidationWhen all case are specified

            if (jsonValidationWhen.then instanceof JsonValidationWhen) {
                return validate((JsonValidationWhen<T>) jsonValidationWhen.then, s);
            } else if (jsonValidationWhen.otherwise instanceof JsonValidationWhen) {
                return validate((JsonValidationWhen<T>) jsonValidationWhen.otherwise, s);
            }

            Object v = get(s, jsonValidationWhen.isField);
            if (jsonValidationWhen.is.validate(v).isValid()) {
                v = get(s, field);
                return ValidationUtils.transferValidationInputToFieldFunc.apply(jsonValidationWhen.then.validate(v),
                                                                                field);
            } else {
                v = get(s, field);
                return ValidationUtils.transferValidationInputToFieldFunc.apply(
                    jsonValidationWhen.otherwise.validate(v), field);
            }
        } else {
            return ValidationResult.invalid(ValidationError.builder()
                                                           .errorCode(NubeException.ErrorCode.INITIALIZER_ERROR)
                                                           .message(
                                                               "JsonValidationWhen validation Initialization Error"));
        }
    }

    public JsonValidationWhen<T> registerField(String field) {
        this.field = field;
        return this;
    }

    public JsonValidationWhen<T> registerIs(String field, Validation<Object> is) {
        inputs.add(1);
        this.isField = field;
        this.is = is;
        return this;
    }

    public JsonValidationWhen<T> registerThen(Validation<Object> then) {
        inputs.add(2);
        this.then = then;
        return this;
    }

    public JsonValidationWhen<T> registerOtherwise(Validation<Object> otherwise) {
        inputs.add(3);
        this.otherwise = otherwise;
        return this;
    }

}
