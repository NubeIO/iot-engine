package com.nubeiot.core.sql.transaction;

import org.jooq.Configuration;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.dto.DataTransferObject.Headers;

public final class TransactionConfigurationCache
    extends AbstractLocalCache<String, Configuration, TransactionConfigurationCache> {

    @Override
    protected String keyLabel() {
        return Headers.X_CORRELATION_ID;
    }

    @Override
    protected String valueLabel() {
        return "Jooq Configuration";
    }

}
