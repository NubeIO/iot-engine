package com.nubeiot.edge.connector.modbus.message;

import com.nubeiot.edge.connector.modbus.json.Message;

public interface ValueOperation extends Message {
    Connection connection();

    int offset();

}
