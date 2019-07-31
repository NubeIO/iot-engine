package com.nubeiot.core.workflow;

import com.nubeiot.core.workflow.ServiceRecord.HttpClientRecord;

public interface HttpConsumerService<D> extends ConsumerService<D> {

    @Override
    HttpClientRecord record();

}
