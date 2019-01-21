package com.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Loop extends Validation<JsonArray, JsonArray> {

    private final ValidationSupplier<Object> supplier;
    private final String parentField;

    @Override
    public Single<ValidationResult<JsonArray>> validate(JsonArray s) {
        final List<Object> objects = new ArrayList<>();
        //  When isRequired is false then parsed JsonArray value will be of size 0
        if (s.size() == 0) {
            return new ValidationResult<JsonArray>().asyncSuccess(s);
        }
        return Observable.fromIterable(s).flatMapSingle(o -> {
            Single<ValidationResult<?>> output = supplier.get(o, this.parentField + "[" + objects.size() + "]");
            objects.add(o);
            return output;
        }).toList().flatMap(r -> new ValidationResult<JsonArray>().asyncSuccess(s));
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
