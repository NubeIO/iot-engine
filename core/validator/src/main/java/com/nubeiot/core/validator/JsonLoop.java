package com.nubeiot.core.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.utils.ValidationUtils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonLoop<T> extends JsonValidationHolder<T> {

    protected final String parentField;
    private List<JsonLoop<Object>> jsonLoops = new ArrayList<>();
    private String previousParentField;

    public void add(@NonNull JsonLoop<Object> validation) {
        jsonLoops.add(validation);
    }

    @Override
    public ValidationResult validate(T data) {
        return validate(this, data);
    }

    protected ValidationResult validate(JsonLoop<?> looper, T data) {
        if (nullable() && data == null) {
            return ValidationResult.valid();
        }

        List<ValidationResult> validationResults = new ArrayList<>();

        Object value = this.get(data, looper.parentField);
        if (!(value instanceof JsonArray || value instanceof JsonObject)) {
            return ValidationResult.invalid(ValidationError.builder()
                                                           .value(looper.parentField)
                                                           .message("is not the type of JsonArray || JsonObject"));
        }

        Iterable v = (Iterable) value;
        int i = 0;
        for (final Object o : v) {
            Object loopedValue;
            String actualParentField =
                (Strings.isNotBlank(looper.previousParentField) ? looper.previousParentField + "." : "") +
                (Strings.isNotBlank(looper.parentField) ? looper.parentField + "." : "");

            if (o instanceof Map.Entry<?, ?>) {
                Map.Entry<?, ?> keyValuePair = (Map.Entry<?, ?>) o;
                actualParentField += keyValuePair.getKey();
                loopedValue = keyValuePair.getValue();
            } else {
                actualParentField += "[" + i + "]";
                i++;
                loopedValue = o;
            }

            for (Map.Entry<String, List<Validation<Object>>> entry : looper.validations.entrySet()) {
                String key = entry.getKey();
                List<Validation<Object>> innerValidations = entry.getValue();
                Object validateValue = get(loopedValue, key);
                for (Validation<Object> validation : innerValidations) {
                    String actualField = Strings.isNotBlank(key) ? actualParentField + "." + key : actualParentField;
                    validationResults.add(
                        ValidationUtils.transferValidationInputToFieldFunc.apply(validation.validate(validateValue),
                                                                                 actualField));
                }
            }

            for (JsonLoop<Object> validation : looper.jsonLoops) {
                validation.previousParentField = actualParentField;
                validationResults.add(validation.validate(validation, this.get(loopedValue, validation.parentField)));
            }
        }

        return ValidationUtils.mergeValidationResultsFunc.apply(validationResults);
    }

}
