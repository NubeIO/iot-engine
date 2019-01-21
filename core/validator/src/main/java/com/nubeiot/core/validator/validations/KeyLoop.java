package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyLoop extends Validation<JsonObject, JsonObject> {

    private final ValidationSupplier<Object> supplier;
    private final String parentField;

    @Override
    public Single<ValidationResult<JsonObject>> validate(JsonObject s) {

        return Observable.fromIterable(s)
                         .flatMapSingle(o -> supplier.get(o.getValue(), parentField + "." + o.getKey()))
                         .toList()
                         .flatMap(r -> new ValidationResult<JsonObject>().asyncSuccess(s));
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
