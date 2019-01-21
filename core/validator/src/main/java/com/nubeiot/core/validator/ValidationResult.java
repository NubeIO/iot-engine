package com.nubeiot.core.validator;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.enums.ValidationState;

import io.reactivex.Single;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ValidationResult<T> {

    private ValidationState validity;
    private String reason;
    private T data;

    public ValidationResult<T> success(T data) {
        this.validity = ValidationState.VALID;
        this.reason = null;
        this.data = data;
        return this;
    }

    public ValidationResult<T> success() {
        return success(null);
    }

    public NubeException invalid(String reason) {
        return new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, reason);
    }

    public Single<ValidationResult<T>> asyncSuccess(T data) {
        return Single.just(success(data));
    }

    public Single<ValidationResult<T>> asyncSuccess() {
        return Single.just(success());
    }

    public Single<ValidationResult<T>> asyncInvalid(String reason) {
        return Single.error(new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, reason));
    }

    public Single<ValidationResult<T>> asyncInvalid(NubeException.ErrorCode errorCode, String reason) {
        return Single.error(new NubeException(errorCode, reason));
    }

}
