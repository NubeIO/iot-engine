package com.nubeiot.core.validator;

import io.reactivex.Single;

public interface ValidationSupplier<T> {

    Single<ValidationResult<?>> get(T s, String parentField);

}
