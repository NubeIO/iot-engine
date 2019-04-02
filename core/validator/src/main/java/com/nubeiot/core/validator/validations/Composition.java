package com.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Composition<T> implements Validation<T> {

    private final List<Validation<T>> validations;
    private BiFunction<ValidationResult, String, ValidationError.Builder> compositionValidationBuilderFunc
        = (validationResult, value) -> {
        StringBuilder message = new StringBuilder();
        NubeException.ErrorCode errorCode = NubeException.ErrorCode.INVALID_ARGUMENT;
        for (int i = 0; i < validationResult.getErrors().size(); i++) {
            ValidationError.Builder error = validationResult.getErrors().get(i);
            // clearing errorType and value
            NubeException nubeException = error.errorType("").value("").build().execute();
            message.append(nubeException.getMessage());
            if (i != validationResult.getErrors().size() - 1) {
                message.append(" && ");
            }
            if (errorCode != nubeException.getErrorCode()) {
                errorCode = nubeException.getErrorCode();
            }
        }
        return ValidationError.builder().value(value).message(message.toString()).errorCode(errorCode);
    };

    @Override
    public ValidationResult validity(T s) {
        List<ValidationResult> validationResults = new ArrayList<>();
        for (Validation<T> validation : validations) {
            validationResults.add(validation.validate(s));
        }

        if (validationResults.stream().filter(ValidationResult::isValid).collect(Collectors.toList()).size() ==
            validations.size()) {

            return ValidationResult.valid();
        }
        return ValidationResult.invalid(
            compositionValidationBuilderFunc.apply(ValidationUtils.mergeValidationResultsFunc.apply(validationResults),
                                                   s == null ? null : s.toString()));
    }

    @Override
    public boolean nullable() {
        return false;
    }

}
