package com.nubeiot.core.validator.validations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegexValidation<T> implements Validation<T> {

    private final String regex;

    @Override
    public ValidationResult validity(T s) {
        Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s.toString());
        boolean isValid = matcher.matches();

        return isValid
               ? ValidationResult.valid()
               : ValidationResult.invalid(
                   ValidationError.builder().value(s.toString()).message("does not match with the pattern " + regex));
    }

}
