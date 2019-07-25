package com.nubeiot.core.sql;

import com.nubeiot.core.sql.type.TimeAudit;

public interface HasTimeAudit {

    <T extends HasTimeAudit> T setTimeAudit(TimeAudit value);

    TimeAudit getTimeAudit();

}
