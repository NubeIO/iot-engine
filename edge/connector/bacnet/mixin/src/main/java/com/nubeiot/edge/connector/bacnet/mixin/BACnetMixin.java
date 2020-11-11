package com.nubeiot.edge.connector.bacnet.mixin;

import io.github.zero88.utils.Strings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.dto.JsonData;

import lombok.NonNull;

public interface BACnetMixin extends JsonData {

    ObjectMapper MAPPER = JsonData.MAPPER.copy().registerModule(BACnetJsonModule.MODULE);

    /**
     * Standardize BACnet key property with lower-case and separate by {@code -}
     *
     * @param keyProp Given key Property
     * @return standard key
     * @see <a href="https://csimn.com/MHelp-SPX-B/spxb-section-14.html">Object Properties</a>
     */
    //    TODO Not sure it is standard
    static String standardizeKey(@NonNull String keyProp) {
        return Strings.transform(keyProp, false, "-");
    }

    @Override
    default ObjectMapper getMapper() {
        return MAPPER;
    }

}
