package com.nubeiot.core.validator.proposal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

public interface DependencyValidation<T> extends ValidationHolder<T> {

    /**
     * Dependency validations
     * <p>
     * It means after validating specified key then do validate for another
     */
    //TODO: maybe need to detected Circular dependency
    Map<String, Map<String, List<Validation>>> dependencies();

    DependencyValidation add(@NonNull String field, @NonNull Validation validation,
                             @NonNull Map<String, List<Validation>> depends);

    DependencyValidation add(@NonNull String field, @NonNull Validation validation, @NonNull String dependField,
                             Validation... dependValidations);

    @Override
    default ValidationResult<T> validate(T data) {
        //Collect data
        validations().forEach((key, validations) -> {
            Object v = get(data, key);
            validations.forEach(validation -> validation.validate(v));
            dependencies().getOrDefault(key, new HashMap<>()).forEach((k2, validations2) -> {
                Object v2 = get(data, k2);
                validations2.forEach(validation -> validation.validate(v2));
            });
        });
        return null;
    }

}
