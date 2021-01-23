package com.nubeiot.core.workflow;

import io.reactivex.annotations.Experimental;
import io.vertx.core.Handler;

@Experimental
public interface ConsumerService<D> extends Handler<D> {

    ServiceRecord record();

}
