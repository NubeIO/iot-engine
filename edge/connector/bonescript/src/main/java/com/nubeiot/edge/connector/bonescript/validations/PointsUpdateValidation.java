package com.nubeiot.edge.connector.bonescript.validations;

import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.COV;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.ID;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.KIND;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.OFFSET;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PERIODIC;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRECISION;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.PRIORITY;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.SCALE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TAGS;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.TYPE;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.UNIT;
import static com.nubeiot.edge.connector.bonescript.constants.DittoAttributes.VALUE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import com.nubeiot.core.validator.JsonConverter;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;
import com.nubeiot.core.validator.ValidationSupplier;
import com.nubeiot.core.validator.validations.Alternative;
import com.nubeiot.core.validator.validations.Any;
import com.nubeiot.core.validator.validations.Composition;
import com.nubeiot.core.validator.validations.Contains;
import com.nubeiot.core.validator.validations.Exist;
import com.nubeiot.core.validator.validations.Forbidden;
import com.nubeiot.core.validator.validations.IntegerValidation;
import com.nubeiot.core.validator.validations.Loop;
import com.nubeiot.core.validator.validations.Min;
import com.nubeiot.core.validator.validations.NumberOrStringValidation;
import com.nubeiot.core.validator.validations.NumberValidation;
import com.nubeiot.core.validator.validations.Range;
import com.nubeiot.core.validator.validations.RegexValidation;
import com.nubeiot.core.validator.validations.Required;
import com.nubeiot.core.validator.validations.StringValidation;
import com.nubeiot.core.validator.validations.When;
import com.nubeiot.edge.connector.bonescript.SingletonBBPinMapping;
import com.nubeiot.edge.connector.bonescript.constants.BBPinMapping;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;

public class PointsUpdateValidation<T> extends Validation<T, JsonArray> {

    @Override
    public Single<ValidationResult<JsonArray>> validity(T s) {
        Validation<JsonArray, JsonArray> pointsLoop = new Loop<>(new PointsValidationSupplier<>());

        return JsonConverter.validate(pointsLoop, s, "");
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    class PointsValidationSupplier<U> implements ValidationSupplier<U> {

        @Override
        public Single<ValidationResult<?>> get(U s, String parentField) {
            BBPinMapping bbPinMapping = SingletonBBPinMapping.getInstance();

            Validation<?, ?> sv = new StringValidation<>();
            Validation<?, ?> requiredStringValidation = new Required<>(sv);

            // type validation
            Single<ValidationResult<Object>> typeWhenAnalogOutputPin;
            Single<ValidationResult<Object>> typeWhenAnalogInPin;
            typeWhenAnalogOutputPin = JsonConverter.validate(new When<>().registerIs(
                JsonConverter.validate(new Contains<>(bbPinMapping.getAnalogOutPins()), s, parentField, ID))
                                                                         .registerThen(JsonConverter.validate(
                                                                             new Contains<>(
                                                                                 bbPinMapping.getOutputTypes()), s,
                                                                             parentField, TYPE))
                                                                         .registerOtherwise(
                                                                             new Forbidden<>().validate()), s, TYPE);
            typeWhenAnalogInPin = JsonConverter.validate(new When<>().registerIs(
                JsonConverter.validate(new Contains<>(bbPinMapping.getAnalogInPins()), s, parentField, ID))
                                                                     .registerThen(JsonConverter.validate(
                                                                         new Contains<>(bbPinMapping.getInputTypes()),
                                                                         s, parentField, TYPE))
                                                                     .registerOtherwise(typeWhenAnalogOutputPin), s,
                                                         TYPE);

            // value validation
            Single<ValidationResult<Object>> valueWhenInputPin;
            Single<ValidationResult<Object>> valueWhenDigitalOutPin;
            Single<ValidationResult<Object>> valueWhenAnalogOutPin;
            valueWhenInputPin = JsonConverter.validate(new When<>().registerIs(
                JsonConverter.validate(new Contains<>(bbPinMapping.getInputPins()), s, parentField, ID))
                                                                   .registerThen(new Forbidden<>().validate())
                                                                   .registerOtherwise(new Any<>().validate()), s,
                                                       VALUE);

            valueWhenDigitalOutPin = JsonConverter.validate(new When<>().registerIs(
                JsonConverter.validate(new Contains<>(bbPinMapping.getDigitalOutPins()), s, parentField, ID))
                                                                        .registerThen(JsonConverter.validate(
                                                                            new Contains<>(
                                                                                bbPinMapping.getValidPinOutputs()), s,
                                                                            parentField, VALUE))
                                                                        .registerOtherwise(valueWhenInputPin), s,
                                                            VALUE);

            valueWhenAnalogOutPin = JsonConverter.validate(new When<>().registerIs(
                JsonConverter.validate(new Contains<>(bbPinMapping.getAnalogOutPins()), s, parentField, ID))
                                                                       .registerThen(JsonConverter.validate(
                                                                           new Alternative<>(Arrays.asList(
                                                                               new Composition<>(Arrays.asList(
                                                                                   new NumberValidation<>(),
                                                                                   new Min<>(0d))), new Contains<>(
                                                                                   bbPinMapping.getValidPinOutputs()))),
                                                                           s, parentField, VALUE))
                                                                       .registerOtherwise(valueWhenDigitalOutPin), s,
                                                           VALUE);

            // priority
            Single<ValidationResult<Object>> priorityWhenExist;
            Single<ValidationResult<Object>> priorityWhenInputPin;

            priorityWhenExist = JsonConverter.validate(
                new When<>().registerIs(JsonConverter.validate(new Exist<>(), s, parentField, PRIORITY))
                            .registerThen(JsonConverter.validate(new Alternative<>(
                                Arrays.asList(new Contains<>(new HashSet<>(Collections.singletonList("null"))),
                                              new Range<>(0d, 16d))), s, parentField, PRIORITY, 16))
                            .registerOtherwise(JsonConverter.validate(new Alternative<>(
                                Arrays.asList(new Contains<>(new HashSet<>(Collections.singletonList("null"))),
                                              new Range<>(0d, 16d))), s, parentField, PRIORITY)), s, PRIORITY);

            priorityWhenInputPin = JsonConverter.validate(new When<>().registerIs(
                JsonConverter.validate(new Contains<>(bbPinMapping.getInputPins()), s, parentField, ID))
                                                                      .registerThen(new Forbidden<>().validate())
                                                                      .registerOtherwise(priorityWhenExist), s,
                                                          PRIORITY);

            Validation<JsonArray, JsonArray> tagsValidation = new Loop<>(new TagsValidationSupplier<>());

            return JsonConverter.validate(requiredStringValidation, s, parentField, ID)
                                .flatMap(
                                    ignored -> JsonConverter.validate(new NumberOrStringValidation<>(), s, parentField,
                                                                      VALUE))
                                .flatMap(ignored -> typeWhenAnalogInPin)
                                .flatMap(ignored -> JsonConverter.validate(sv, s, parentField, "scale"))
                                .flatMap(ignored -> JsonConverter.validate(
                                    new RegexValidation("^-?[0-9]*\\.?[0-9]+\\:-?[0-9]*\\.?[0-9]+$"), s, parentField,
                                    SCALE))
                                .flatMap(ignored -> JsonConverter.validate(new Min<>(0d), s, parentField, PRECISION))
                                .flatMap(
                                    ignored -> JsonConverter.validate(new NumberValidation<>(), s, parentField, OFFSET))
                                .flatMap(ignored -> valueWhenAnalogOutPin)
                                .flatMap(ignored -> priorityWhenInputPin)
                                .flatMap(ignored -> JsonConverter.validate(sv, s, parentField, KIND))
                                .flatMap(ignored -> JsonConverter.validate(sv, s, parentField, UNIT))
                                .flatMap(ignored -> JsonConverter.validate(tagsValidation, s, parentField, TAGS))
                                .flatMap(ignored -> JsonConverter.validate(
                                    new Contains<>(new HashSet<>(Arrays.asList(COV, PERIODIC))), s, parentField,
                                    "historySettings.type"))
                                .flatMap(
                                    ignored -> JsonConverter.validate(sv, s, parentField, "historySettings.schedule"))
                                .flatMap(ignored -> JsonConverter.validate(new Min<>(0d), s, parentField,
                                                                           "historySettings" + ".tolerance"))
                                .flatMap(ignore -> JsonConverter.validate(
                                    new Composition<>(Arrays.asList(new IntegerValidation<>(), new Min<>(0d))), s,
                                    parentField, "historySettings" + ".size"));
        }

        class TagsValidationSupplier<V> implements ValidationSupplier<V> {

            @Override
            public Single<ValidationResult<?>> get(V s, String parentField) {
                return JsonConverter.validate(new StringValidation<>(), s, parentField, "").map(x -> x);
            }

        }

    }

}
