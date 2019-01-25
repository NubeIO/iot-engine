package com.nubeiot.core.validator.validations;

import java.util.ArrayList;
import java.util.List;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Loop<T> extends Validation<T, JsonArray> {

    private final ValidationSupplier<Object> supplier;

    @Override
    public Single<ValidationResult<JsonArray>> validity(T s) {
        if (s instanceof JsonArray) {
            final List<Object> objects = new ArrayList<>();
            //  When isRequired is false then parsed JsonArray value will be of size 0
            JsonArray jsonArray = (JsonArray) s;
            if (jsonArray.size() == 0) {
                return ValidationResult.valid(jsonArray);
            }
            return Observable.fromIterable(jsonArray).flatMapSingle(o -> {

                Single<ValidationResult<?>> output = supplier.get(o, this.input + "[" + objects.size() + "]");
                objects.add(o);
                return output;
            }).toList().flatMap(r -> ValidationResult.valid(jsonArray));
        } else {
            return ValidationResult.invalid(
                Strings.format("{0}: \"{1}\" must be of type JsonArray", getErrorType(), this.input));
        }
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
