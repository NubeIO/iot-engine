package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JArray<T> extends Validation<T, JsonArray> {

    @Override
    public Single<ValidationResult<JsonArray>> validate(T s) {
        if (s instanceof JsonArray) {
            return new ValidationResult<JsonArray>().asyncSuccess((JsonArray) s);
        }
        return new ValidationResult<JsonArray>().asyncInvalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value is not the type of JsonArray", errorType, getAbsoluteField());
    }

}
