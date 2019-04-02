package com.nubeiot.core.validator;

public interface Validation<T> {

    default ValidationResult validate(T data) {
        if (nullable() && data == null) {
            return ValidationResult.valid();
        }
        return validity(data);
    }

    ValidationResult validity(T data);

    default boolean nullable() {
        return true;
    }

}
