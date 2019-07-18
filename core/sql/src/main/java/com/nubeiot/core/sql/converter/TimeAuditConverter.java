package com.nubeiot.core.sql.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.core.utils.Strings;

public final class TimeAuditConverter implements Converter<String, TimeAudit> {

    @Override
    public TimeAudit from(String databaseObject) {
        return Strings.isBlank(databaseObject) ? null : JsonData.from(databaseObject, TimeAudit.class);
    }

    @Override
    public String to(TimeAudit userObject) {
        return Objects.isNull(userObject) ? null : userObject.toJson().encode();
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<TimeAudit> toType() {
        return TimeAudit.class;
    }

}
