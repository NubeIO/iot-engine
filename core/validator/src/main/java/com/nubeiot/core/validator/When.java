package com.nubeiot.core.validator;

import java.util.HashSet;
import java.util.Set;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.core.validator.utils.ValidationUtils;

import io.vertx.core.json.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class When<T> implements Validation<T> {

    private String isField;
    private String thenField;
    private String otherwiseField;

    private Validation<Object> is;
    private Validation<Object> then;
    private Validation<Object> otherwise;

    private Set<Integer> inputs = new HashSet<>();

    public Object get(T data, String key) {
        if (!(data instanceof JsonObject)) {
            return null;
        }
        return JsonUtils.getObject((JsonObject) data, key);
    }

    @Override
    public ValidationResult validity(T s) {
        if (inputs.size() == 3) { // when all case are specified
            Object v = get(s, isField);
            if (is.validate(v).isValid()) {
                v = get(s, thenField);
                return ValidationUtils.transferValidationInputToFieldFunc.apply(then.validate(v), thenField);
            } else {
                v = get(s, otherwiseField);
                if (otherwise instanceof When) {
                    return otherwise.validate(v);
                }
                return ValidationUtils.transferValidationInputToFieldFunc.apply(otherwise.validate(v), otherwiseField);
            }
        } else {
            return ValidationResult.invalid(ValidationError.builder()
                                                           .errorCode(NubeException.ErrorCode.INITIALIZER_ERROR)
                                                           .message("When validation Initialization Error"));
        }
    }

    public void setIs(String field, Validation<Object> is) {
        inputs.add(1);
        this.isField = field;
        this.is = is;
    }

    public void setThen(String field, Validation<Object> then) {
        inputs.add(2);
        this.thenField = field;
        this.then = then;
    }

    public void setOtherwise(String field, Validation<Object> otherwise) {
        inputs.add(3);
        this.otherwiseField = field;
        this.otherwise = otherwise;
    }

    public void setIs(Validation<Object> is) {
        inputs.add(1);
        this.is = is;
    }

    public void setThen(Validation<Object> then) {
        inputs.add(2);
        this.then = then;
    }

    public void setOtherwise(Validation<Object> otherwise) {
        inputs.add(3);
        this.otherwise = otherwise;
    }

}
