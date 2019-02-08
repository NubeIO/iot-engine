package com.nubeiot.core.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeiot.core.exceptions.ValidationError;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    List<ValidationError.Builder> errors = new ArrayList<>();

    public static ValidationResult valid() {
        return new ValidationResult();
    }

    public static ValidationResult invalid(List<ValidationError.Builder> validationErrorBuilders) {
        return new ValidationResult(validationErrorBuilders);
    }

    public static ValidationResult invalid(ValidationError.Builder validationErrorBuilder) {
        return invalid(Collections.singletonList(validationErrorBuilder));
    }

    public boolean isValid() {
        return errors().isEmpty();
    }

    public List<ValidationError.Builder> errors() {
        return errors;
    }

}
