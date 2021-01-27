package com.nubeiot.edge.connector.bacnet.entity;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.iotdata.IoTEntity;

public interface BACnetEntity<K> extends BACnetProtocol, IoTEntity<K> {}
