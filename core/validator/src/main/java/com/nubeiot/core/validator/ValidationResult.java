package com.nubeiot.core.validator;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ValidationError;

import io.reactivex.Single;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationResult<T> {

    private T data;

    public static <E> Single<ValidationResult<E>> valid() {
        ValidationResult<E> validationResult = new ValidationResult<>();
        return Single.just(validationResult);
    }

    public static <E> Single<ValidationResult<E>> valid(E data) {
        ValidationResult<E> validationResult = new ValidationResult<>();
        validationResult.data = data;
        return Single.just(validationResult);
    }

    public static <E> Single<ValidationResult<E>> invalid(NubeException.ErrorCode errorCode, String message) {
        return Single.error(new ValidationError(errorCode, message));
    }

    public static <E> Single<ValidationResult<E>> invalid(String message) {
        return Single.error(new ValidationError(message));
    }

}
