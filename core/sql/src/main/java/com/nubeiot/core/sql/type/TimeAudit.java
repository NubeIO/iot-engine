package com.nubeiot.core.sql.type;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.DateTimes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonNaming(SnakeCaseStrategy.class)
@ToString
public final class TimeAudit implements JsonData {

    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime lastModifiedTime;
    private String lastModifiedBy;

    public static TimeAudit created(String createdBy) {
        return new TimeAudit(DateTimes.now(), createdBy, null, null);
    }

    public static TimeAudit modified(TimeAudit timeAudit, String lastModifiedBy) {
        if (Objects.isNull(timeAudit)) {
            return new TimeAudit(null, null, DateTimes.now(), lastModifiedBy);
        }
        timeAudit.setLastModifiedBy(lastModifiedBy);
        timeAudit.setLastModifiedTime(DateTimes.now());
        return timeAudit;
    }

}
