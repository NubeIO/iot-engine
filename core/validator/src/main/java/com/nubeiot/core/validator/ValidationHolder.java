package com.nubeiot.core.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.validator.utils.ValidationUtils;

import lombok.NonNull;

/**
 * For map, json object, or POJO
 *
 * @param <T>
 */
public interface ValidationHolder<T> extends Validation<T> {

    Map<String, List<Validation<Object>>> validations();

    ValidationHolder add(String field, @NonNull Validation<Object> validation);

    Object get(Object data, String field);

    default ValidationHolder add(String field, Validation<Object>... validations) {
        Arrays.stream(validations).filter(Objects::nonNull).forEach(validation -> add(field, validation));
        return this;
    }

    @Override
    default ValidationResult validate(T data) {
        final List<ValidationResult> validationResults = new ArrayList<>();
        validations().forEach((field, validations) -> {
            Object v = get(data, field);
            validations.forEach(validation -> validationResults.add(
                ValidationUtils.transferValidationInputToFieldFunc.apply(validation.validate(v), field)));
        });
        return ValidationUtils.mergeValidationResultsFunc.apply(validationResults);
    }

    @Override
    default ValidationResult validity(T data) {
        return null;
    }

    // todo: we will work on this
    default ValidationResult eagerValidate(T data) {
        for (Map.Entry<String, List<Validation<Object>>> entry : validations().entrySet()) {
            String field = entry.getKey();
            List<Validation<Object>> validations = entry.getValue();
            Object v = get(data, field);
            for (Validation<Object> validation : validations) {
                ValidationResult validationResult = validation.validate(v);
                if (!validationResult.errors().isEmpty()) {
                    return ValidationUtils.transferValidationInputToFieldFunc.apply(validationResult, field);
                }
            }
        }
        return ValidationResult.valid();
    }

}
