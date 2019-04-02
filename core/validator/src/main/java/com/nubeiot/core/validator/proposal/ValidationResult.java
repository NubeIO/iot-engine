package com.nubeiot.core.validator.proposal;

import java.util.List;

public interface ValidationResult<T> {

    T data();

    default boolean isValid() {
        return errors().isEmpty();
    }

    List<String> errors();

}
