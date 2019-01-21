package com.nubeiot.core.validator.validations;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Ptrn extends Validation<String, String> {

    private final String regex;

    @Override
    public Single<ValidationResult<String>> validate(String s) {
        Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        boolean isValid = matcher.matches();

        return isValid
               ? new ValidationResult<String>().asyncSuccess((s))
               : new ValidationResult<String>().asyncInvalid(getErrorMessage());
    }

    @Override
    public String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value does not match with the pattern \"{2}\"", errorType,
                              getAbsoluteField(), regex);
    }

}
