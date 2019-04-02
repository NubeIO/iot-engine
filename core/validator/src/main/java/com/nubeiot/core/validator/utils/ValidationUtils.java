package com.nubeiot.core.validator.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.validator.ValidationResult;

public class ValidationUtils {

    public static Function<List<ValidationError.Builder>, NubeException> convertValidationErrorsToException
        = (List<ValidationError.Builder> errorBuilders) -> {
        StringBuilder message = new StringBuilder();
        NubeException.ErrorCode code = NubeException.ErrorCode.INVALID_ARGUMENT;
        for (int i = 0; i < errorBuilders.size(); i++) {
            NubeException nubeException = errorBuilders.get(i).build().execute();
            message.append(nubeException.getMessage());
            if (code != nubeException.getErrorCode()) {
                code = nubeException.getErrorCode();
            }
            if (i < errorBuilders.size() - 1) {
                message.append(" && ");
            }
        }
        return new NubeException(code, message.toString());
    };

    public static BiFunction<ValidationResult, String, ValidationResult> transferValidationInputToFieldFunc
        = (validationResult, field) -> {
        validationResult.errors().forEach(error -> error.value(field));
        return validationResult;
    };

    public static Function<List<ValidationResult>, ValidationResult> mergeValidationResultsFunc
        = (validationResults) -> {
        List<ValidationError.Builder> validationErrors = new ArrayList<>();
        for (ValidationResult validationResult : validationResults) {
            if (!validationResult.errors().isEmpty()) {
                validationErrors.addAll(validationResult.errors());
            }
        }
        return new ValidationResult(validationErrors);
    };

}
