package com.nubeiot.core.sql.converter;

import java.util.Objects;

import org.jooq.Converter;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.type.SyncAudit;
import com.nubeiot.core.utils.Strings;

public final class SyncAuditConverter implements Converter<String, SyncAudit> {

    @Override
    public SyncAudit from(String databaseObject) {
        return Strings.isBlank(databaseObject) ? null : JsonData.from(databaseObject, SyncAudit.class);
    }

    @Override
    public String to(SyncAudit userObject) {
        return Objects.isNull(userObject) ? null : userObject.toJson().encode();
    }

    @Override
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    public Class<SyncAudit> toType() {
        return SyncAudit.class;
    }

}
