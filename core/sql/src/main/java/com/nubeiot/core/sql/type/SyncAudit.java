package com.nubeiot.core.sql.type;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.DateTimes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonNaming(SnakeCaseStrategy.class)
@ToString
public final class SyncAudit implements JsonData {

    private OffsetDateTime syncedTime;
    private boolean synced;

    public static SyncAudit notSynced() {
        return new SyncAudit(null, false);
    }

    public static SyncAudit synced() {
        return new SyncAudit(DateTimes.now(), true);
    }

}
