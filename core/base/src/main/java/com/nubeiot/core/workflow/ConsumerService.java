package com.nubeiot.core.workflow;

import io.vertx.core.Handler;

public interface ConsumerService<D> extends Handler<D> {

    ServiceRecord record();

}
