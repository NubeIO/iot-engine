package com.nubeiot.core.validator.validations;

import java.util.Set;

import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.vertx.core.json.JsonArray;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class Contains<T> implements Validation<T> {

    private final Set<?> items;
    private boolean strict = true;

    @Override
    public ValidationResult validity(T s) {
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
        return contains ? ValidationResult.valid() : ValidationResult.invalid(buildErrorMessage(s));
    }

    private ValidationError.Builder buildErrorMessage(T s) {
        return strict
               ? ValidationError.builder().value(s.toString()).message("strictly should fall in the " + items)
               : ValidationError.builder().value(s.toString()).message("should fall in the " + items);
    }

}
