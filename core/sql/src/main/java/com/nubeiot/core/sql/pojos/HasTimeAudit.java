package com.nubeiot.core.sql.pojos;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.type.TimeAudit;

public interface HasTimeAudit extends VertxPojo {

    TimeAudit getTimeAudit();

    <T extends HasTimeAudit> T setTimeAudit(TimeAudit value);

}
