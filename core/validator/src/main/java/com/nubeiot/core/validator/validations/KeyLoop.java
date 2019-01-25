package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyLoop<T> extends Validation<T, JsonObject> {

    private final ValidationSupplier<Object> supplier;

    @Override
    public Single<ValidationResult<JsonObject>> validity(T s) {
        if (s instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) s;
            return Observable.fromIterable(jsonObject)
                             .flatMapSingle(o -> supplier.get(o.getValue(), this.input + "." + o.getKey()))
                             .toList()
                             .flatMap(r -> ValidationResult.valid(jsonObject));
        } else {
            return ValidationResult.invalid(
                Strings.format("{0}: \"{1}\" must be of type JsonObject", getErrorType(), this.input));
        }
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
