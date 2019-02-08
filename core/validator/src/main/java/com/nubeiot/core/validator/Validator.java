package com.nubeiot.core.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import com.nubeiot.core.exceptions.ValidationError;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Singular;

@Builder(builderClassName = "Builder")
public class Validator<T> {

    private T defaultValue;
    @Default
    private BiFunction<T, T, T> mergeFunc = (o1, o2) -> Objects.isNull(o1) ? o2 : o1;
    @Default
    private boolean nullable = true;
    @NonNull
    @Singular
    private List<Validation<T>> validations;

    /**
     * Validate data to the end then bundle all errors in {@link ValidationResult}
     *
     * @param data
     * @return
     */
    public ValidationResult execute(T data) {
        T merged = before(data);
        List<ValidationError.Builder> errors = new ArrayList<>();
        for (Validation<T> validation : validations) {
            ValidationResult validationResult = validation.validate(merged);
            if (!validationResult.errors().isEmpty()) {
                errors.addAll(validationResult.errors());
            }
        }
        return new ValidationResult(errors);
    }

    /**
     * Validate data and raise error immediately if encounter any error
     *
     * @param data
     * @return
     */
    public ValidationResult eagerExecute(T data) {
        T merged = before(data);
        for (Validation<T> validation : validations) {
            ValidationResult validationResult;

            validationResult = validation.validate(merged);

            if (!validationResult.errors().isEmpty()) {
                return ValidationResult.invalid(validationResult.errors());
            }
        }
        return ValidationResult.valid();
    }

    private T before(T data) {
        return mergeFunc.apply(data, defaultValue);
    }

}
