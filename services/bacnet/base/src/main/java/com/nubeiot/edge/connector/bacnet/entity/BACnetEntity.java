package com.nubeiot.edge.connector.bacnet.entity;

import io.github.zero88.qwe.iot.data.IoTEntity;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetJsonMixin;

public interface BACnetEntity<K> extends BACnetProtocol, IoTEntity<K>, BACnetJsonMixin {

}
