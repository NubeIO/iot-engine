package com.nubeiot.core.validator.proposal;

/**
 * @param <T>
 */
public interface Validation<T> {

    ValidationResult<T> validate(T data);

}
