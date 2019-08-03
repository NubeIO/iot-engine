package com.nubeiot.core.sql;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.sql.type.Label;

public interface HasLabel extends VertxPojo {

    Label getLabel();

    <T extends HasLabel> T setLabel(Label value);

}
