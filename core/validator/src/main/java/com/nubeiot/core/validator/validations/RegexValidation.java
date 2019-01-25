package com.nubeiot.core.validator.validations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegexValidation extends Validation<String, String> {

    private final String regex;

    @Override
    public Single<ValidationResult<String>> validity(String s) {
        Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        boolean isValid = matcher.matches();

        return isValid ? ValidationResult.valid(s) : ValidationResult.invalid(getErrorMessage());
    }

    @Override
    public String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" does not match with the pattern \"{2}\"", getErrorType(), getInput(),
                              regex);
    }

}
