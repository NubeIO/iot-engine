package com.nubeiot.scheduler.trigger;

import java.text.ParseException;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.ScheduleBuilder;
import org.quartz.TriggerKey;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
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

    private final String expr;
    private final String tz;

    CronTriggerModel(TriggerType type, TriggerKey key, String expr, String tz) {
        super(key, type);
        this.expr = expr;
        this.tz = tz;
    }

    @Override
    protected @NonNull ScheduleBuilder<CronTrigger> scheduleBuilder() {
        TimeZone timeZone = TimeZone.getTimeZone(Strings.isBlank(tz) ? ZoneOffset.UTC.getId() : tz);
        return CronScheduleBuilder.cronSchedule(toCronExpr()).inTimeZone(timeZone);
    }

    private CronExpression toCronExpr() {
        try {
            return new CronExpression(expr);
        } catch (IllegalArgumentException | ParseException e) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Cannot parse cron expression", new HiddenException(e));
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractTriggerBuilder {

        public CronTriggerModel build() {
            return new CronTriggerModel(TriggerType.CRON, TriggerModel.createKey(group, name), expr, tz);
        }

    }

}
