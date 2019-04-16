package com.nubeiot.edge.connector.modbus.message;

import com.nubeiot.edge.connector.modbus.json.Message;

public enum Type {
    READ_INPUT_CONTACT(ReadInputContact.class),
    READ_OUTPUT_COIL(ReadOutputCoil.class),
    WRITE_OUTPUT_COIL(WriteOutputCoil.class),
    BOOLEAN_VALUE(BooleanValue.class),
    INTEGER_VALUE(IntegerValue.class),
    FLOAT_VALUE(FloatValue.class),
    READ_INPUT_REGISTER_INTEGER(ReadInputRegisterInteger.class),
    READ_INPUT_REGISTER_FLOAT(ReadInputRegisterFloat.class),
    READ_HOLDING_REGISTER_INTEGER(ReadHoldingRegisterInteger.class),
    READ_INPUT_REGISTER_STRING(ReadInputRegisterString.class),
    READ_HOLDING_REGISTER_FLOAT(ReadHoldingRegisterFloat.class),
    READ_HOLDING_REGISTER_STRING(ReadHoldingRegisterString.class),
    WRITE_HOLDING_REGISTER_INTEGER(WriteHoldingRegisterInteger.class),
    WRITE_HOLDING_REGISTER_FLOAT(WriteHoldingRegisterFloat.class),
    WRITE_HOLDING_REGISTER_STRING(WriteHoldingRegisterString.class),
    ;

    public Class<? extends Message> messageClass;

    <T extends Message> Type(Class<T> messageClass) {
        this.messageClass = messageClass;
    }
}
