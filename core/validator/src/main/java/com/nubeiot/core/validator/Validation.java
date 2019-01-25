package com.nubeiot.core.validator;

import com.nubeiot.core.exceptions.NubeException;

import io.reactivex.Single;
import lombok.Getter;
import lombok.Setter;

public abstract class Validation<T, R> {

    @Getter
    protected String input = "";

    @Getter
    @Setter
    private String errorType = "ValidationError";
    @Getter
    @Setter
    private NubeException.ErrorCode errorCode = NubeException.ErrorCode.INVALID_ARGUMENT;
    private boolean overrideInput = true;

    /**
     * @param s input value
     * @return validation result
     */
    public final Single<ValidationResult<R>> validate(T s) {
        // May be default value also be failed on the validation
        if (isNullable() && s == null) {
            return ValidationResult.valid();
        }

        // Set input only if no DI occurred from parent on Method: registerInput(String input)
        if (overrideInput) {
            this.input = s != null ? s.toString() : "";
        }

        return this.validity(s);
    }

    /**
     * This method contains the actual business logic for the individual validation
     *
     * @param s input value
     * @return validation result
     */
    public abstract Single<ValidationResult<R>> validity(T s);

    protected abstract String getErrorMessage();

    /**
     * @param input input value for displaying error value if exist
     * @return own class object
     */
    public Validation<T, R> registerInput(String input) {
        overrideInput = false;
        this.input = input;
        return this;
    }

    protected boolean isNullable() {
        return true;
    }

}
