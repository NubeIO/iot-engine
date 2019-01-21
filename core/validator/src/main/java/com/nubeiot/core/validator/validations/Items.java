package com.nubeiot.core.validator.validations;

import java.util.List;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Items<T> extends Validation<T, T> {

    private final List<?> items;

    @Override
    public Single<ValidationResult<T>> validate(T s) {
        boolean contains = true;
        if (s instanceof JsonArray) {
            for (Object o : (JsonArray) s) {
                if (!items.contains(o)) {
                    contains = false;
                    break;
                }
            }
        } else {
            contains = items.contains(s);
        }
        return contains
               ? new ValidationResult<T>().asyncSuccess(s)
               : new ValidationResult<T>().asyncInvalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value does not fall on the items {2}", errorType, getAbsoluteField(),
                              items);
    }

}
