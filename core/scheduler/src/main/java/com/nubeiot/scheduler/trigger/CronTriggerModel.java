package com.nubeiot.scheduler.trigger;

import java.text.ParseException;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.TimeZone;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.ScheduleBuilder;
import org.quartz.TriggerKey;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.scheduler.trigger.TriggerModel.AbstractTriggerModel;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = CronTriggerModel.Builder.class)
public final class CronTriggerModel extends AbstractTriggerModel {

    @NonNull
    private final CronExpression expression;
    @NonNull
    private final TimeZone timezone;

    private CronTriggerModel(TriggerType type, TriggerKey key, CronExpression expression, TimeZone timezone) {
        super(key, type);
        this.expression = expression;
        this.timezone = timezone;
    }

    @Override
    protected @NonNull ScheduleBuilder<CronTrigger> scheduleBuilder() {
        return CronScheduleBuilder.cronSchedule(expression).inTimeZone(timezone);
    }

    @Override
    public JsonObject toJson() {
        return super.toJson().put("timezone", timezone.getID()).put("expression", expression.getCronExpression());
    }

    @Override
    public String toString() {
        return Strings.format("Expr: \"{0}\" - TimeZone: \"{1}\"", expression, timezone.getID());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractTriggerModelBuilder<CronTriggerModel, Builder> {

        private String expr;
        private String tz;

        static CronExpression toCronExpr(String expression) {
            try {
                return new CronExpression(expression);
            } catch (IllegalArgumentException | ParseException e) {
                throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Cannot parse cron expression",
                                        new HiddenException(e));
            }
        }

        public Builder tz(String timezone) {
            this.tz = timezone;
            return this;
        }

        public Builder expr(String expression) {
            this.expr = expression;
            return this;
        }

        public CronTriggerModel build() {
            timezone = Objects.nonNull(timezone)
                       ? timezone
                       : TimeZone.getTimeZone(Strings.isBlank(tz) ? ZoneOffset.UTC.getId() : tz);
            expression = Objects.nonNull(expression) ? expression : toCronExpr(expr);
            return new CronTriggerModel(TriggerType.CRON, key(), expression, timezone);
        }

    }

}
