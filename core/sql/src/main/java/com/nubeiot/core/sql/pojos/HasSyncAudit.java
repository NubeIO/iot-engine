package com.nubeiot.core.sql.pojos;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.type.SyncAudit;

public interface HasSyncAudit extends VertxPojo {

    SyncAudit getSyncAudit();

    <T extends HasSyncAudit> T setSyncAudit(SyncAudit value);

}
