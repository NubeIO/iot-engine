package com.nubeiot.core.validator.proposal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.NonNull;

/**
 * For map, json object, or POJO
 *
 * @param <T>
 */
public interface ValidationHolder<T> extends Validation<T> {

    Map<String, List<Validation>> validations();

    ValidationHolder add(String field, @NonNull Validation validation);

    Object get(T data, String key);

    default ValidationHolder add(String field, Validation... validations) {
        Arrays.stream(validations).filter(Objects::nonNull).forEach(validation -> add(field, validation));
        return this;
    }

    @Override
    default ValidationResult<T> validate(T data) {
        //Collect data
        validations().forEach((s, validations) -> {
            Object v = get(data, s);
            validations.forEach(validation -> validation.validate(v));
        });
        return null;
    }

}
