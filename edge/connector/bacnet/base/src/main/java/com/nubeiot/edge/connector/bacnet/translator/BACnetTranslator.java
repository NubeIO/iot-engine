package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.translator.IoTTranslator;

import lombok.NonNull;

public interface BACnetTranslator<T, U> extends IoTTranslator<T, U> {

    @Override
    default @NonNull Protocol protocol() {
        return Protocol.BACNET;
    }

}
