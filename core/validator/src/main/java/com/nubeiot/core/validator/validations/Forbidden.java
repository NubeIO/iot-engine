package com.nubeiot.core.validator.validations;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

public class Forbidden<T> implements Validation<T> {

    public ValidationResult validate() {
        return this.validate(null);
    }

    @Override
    public ValidationResult validity(T s) {
        return ValidationResult.invalid(ValidationError.builder()
                                                       .errorType("Forbidden")
                                                       .errorCode(NubeException.ErrorCode.INSUFFICIENT_PERMISSION_ERROR)
                                                       .message("you are not authorized to post this value"));
    }

    @Override
    public boolean nullable() {
        return false;
    }

}
