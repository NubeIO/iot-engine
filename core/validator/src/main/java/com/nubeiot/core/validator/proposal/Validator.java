package com.nubeiot.core.validator.proposal;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Singular;

@Builder(builderClassName = "Builder")
public class Validator<T> {

    private T defaultValue;
    //TODO introduce some static BiFunction such as merge json
    @Default
    private BiFunction<T, T, T> mergeFunc = (t, t2) -> Objects.isNull(t2) ? t : t2;
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
    public ValidationResult<T> execute(T data) {
        T merged = before(data);
        // TODO: Collect and merge all errors if any at here
        validations.forEach(validation -> validation.validate(merged));
        return null;
    }

    /**
     * Validate data and raise error immediately if encounter any error
     *
     * @param data
     * @return
     */
    public ValidationResult<T> eagerExecute(T data) {
        T merged = before(data);
        return null;
    }

    private T before(T data) {
        T d = mergeFunc.apply(defaultValue, data);
        if (Objects.isNull(d) && !nullable) {
            // or return ValidationResult
            // throw new ValidationError("non null");
        }
        return d;
    }

}
