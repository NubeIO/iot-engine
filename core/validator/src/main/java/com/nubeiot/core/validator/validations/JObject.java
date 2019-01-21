package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JObject<T> extends Validation<T, JsonObject> {

    @Override
    public Single<ValidationResult<JsonObject>> validate(T s) {
        if (s instanceof JsonObject) {
            return new ValidationResult<JsonObject>().asyncSuccess((JsonObject) s);
        }
        return new ValidationResult<JsonObject>().asyncInvalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value is not the type of JsonObject", errorType, getAbsoluteField());
    }

}
