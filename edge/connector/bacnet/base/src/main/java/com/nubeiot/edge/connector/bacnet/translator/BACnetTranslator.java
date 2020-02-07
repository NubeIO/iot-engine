package com.nubeiot.edge.connector.bacnet.translator;

import com.nubeiot.iotdata.dto.IoTNotion;
import com.nubeiot.iotdata.dto.IoTNotion.IoTChunkNotion;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.translator.IoTNotionTranslator;
import com.nubeiot.iotdata.translator.IoTTranslator;
import com.serotonin.bacnet4j.type.Encodable;

import lombok.NonNull;

public interface BACnetTranslator<T, U> extends IoTTranslator<T, U> {

    @Override
    default @NonNull Protocol protocol() {
        return Protocol.BACNET;
    }

    interface BACnetIoTNotionTranslator<T extends IoTNotion, U extends Encodable>
        extends BACnetTranslator<T, U>, IoTNotionTranslator<T, U> {

    }


    interface BACnetIoTChunkNotionTranslator<T extends IoTChunkNotion, U extends Encodable>
        extends BACnetTranslator<T, U>, IoTNotionTranslator<T, U> {

        @Override
        default U deserialize(T concept) {
            throw new UnsupportedOperationException();
        }

    }

}
