package com.nubeiot.core.validator.validations;

import java.util.Set;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class Contains<T> extends Validation<T, T> {

    private final Set<?> items;
    private boolean strict = true;

    @Override
    public Single<ValidationResult<T>> validity(T s) {
        boolean contains = false;
        if (s instanceof JsonArray) {
            for (Object o : (JsonArray) s) {
                if (items.contains(o)) {
                    contains = true;
                    if (!strict) {
                        break;
                    }
                } else {
                    contains = false;
                    if (strict) {
                        break;
                    }
                }
            }
        } else {
            contains = items.contains(s);
        }
        return contains ? ValidationResult.valid(s) : ValidationResult.invalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return strict
               ? Strings.format("{0}: \"{1}\" strictly should fall in the {2}", getErrorType(), getInput(), items)
               : Strings.format("{0}: \"{1}\" should fall in the {2}", getErrorType(), getInput(), items);
    }

}
