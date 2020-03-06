package com.nubeiot.core.workflow;

import io.reactivex.annotations.Experimental;

import com.nubeiot.core.workflow.ServiceRecord.HttpClientRecord;

@Experimental
public interface HttpConsumerService<D> extends ConsumerService<D> {

    @Override
    HttpClientRecord record();

}
