package com.nubeiot.edge.connector.modbus;

import com.nubeiot.edge.connector.modbus.message.FloatOperation;
import com.nubeiot.edge.connector.modbus.message.IntegerOperation;
import com.nubeiot.edge.connector.modbus.json.Message;
import com.serotonin.modbus4j.code.DataType;

import static com.serotonin.modbus4j.code.RegisterRange.*;

public class MessageHelpers {
    /**
     * Maps the configuration  of an float read message (byte order etc.) to the {@link DataType} that represents this configuration
     * in the modbus4j library.
     *
     * @param message Message that contains integer parsing information
     * @return The {@link DataType} int that represents the configuration
     * @throws IllegalArgumentException if the passed message cannot be represented by a DataType.
     */
    public static int dataTypeForFloatOperation(FloatOperation message) {
        if (message.byteCount() == 4) {
            if (message.swapRegisters()) {
                return DataType.FOUR_BYTE_FLOAT_SWAPPED;
            } else {
                return DataType.FOUR_BYTE_FLOAT;
            }
        }
        if (message.byteCount() == 8) {
            if (message.swapRegisters()) {
                return DataType.EIGHT_BYTE_FLOAT_SWAPPED;
            } else {
                return DataType.EIGHT_BYTE_FLOAT;
            }
        }
        throw new IllegalArgumentException("Unknown float format configuration: " + message.toString());
    }

    /**
     * Maps the configuration  of an integer read message (byte order etc.) to the {@link DataType} that represents this configuration
     * in the modbus4j library.
     *
     * @param message Message that contains integer parsing information
     * @return The {@link DataType} int that represents the configuration
     * @throws IllegalArgumentException if the passed message cannot be represented by a DataType.
     */
    public static int dataTypeForIntegerOperation(IntegerOperation message) {
        if (message.byteCount() == 1) {
            if (message.swapBytes()) {
                return DataType.ONE_BYTE_INT_UNSIGNED_LOWER;
            } else {
                return DataType.ONE_BYTE_INT_UNSIGNED_UPPER;
            }
        }
        if (message.byteCount() == 2) {
            if (message.unsigned()) {
                if (message.swapBytes()) {
                    return DataType.TWO_BYTE_INT_UNSIGNED_SWAPPED;
                } else {
                    return DataType.TWO_BYTE_INT_UNSIGNED;
                }
            } else {
                if (message.swapBytes()) {
                    return DataType.TWO_BYTE_INT_SIGNED_SWAPPED;
                } else {
                    return DataType.TWO_BYTE_INT_SIGNED;
                }
            }
        } else if (message.byteCount() == 4) {
            if (message.unsigned()) {
                if (message.swapBytes()) {
                    if (message.swapRegisters()) {
                        return DataType.FOUR_BYTE_INT_UNSIGNED_SWAPPED_SWAPPED;
                    } else {
                        return DataType.FOUR_BYTE_INT_UNSIGNED_SWAPPED;
                    }
                } else {
                    return DataType.FOUR_BYTE_INT_UNSIGNED;
                }
            } else {
                if (message.swapBytes()) {
                    if (message.swapRegisters()) {
                        return DataType.FOUR_BYTE_INT_SIGNED_SWAPPED_SWAPPED;
                    } else {
                        return DataType.FOUR_BYTE_INT_SIGNED_SWAPPED;
                    }
                } else {
                    return DataType.FOUR_BYTE_INT_SIGNED;
                }
            }
        } else if (message.byteCount() == 8) {
            if (message.unsigned()) {
                if (message.swapBytes()) {
                    return DataType.EIGHT_BYTE_INT_UNSIGNED_SWAPPED;
                } else {
                    return DataType.EIGHT_BYTE_INT_UNSIGNED;
                }
            } else {
                if (message.swapBytes()) {
                    return DataType.EIGHT_BYTE_INT_SIGNED_SWAPPED;
                } else {
                    return DataType.EIGHT_BYTE_INT_SIGNED;
                }
            }
        }
        throw new IllegalArgumentException("Unknown integer format configuration: " + message.toString());
    }

    /**
     * Returns the Register as modbus4j integer that is associated with a certain message.
     *
     * @param message The message to evaluate
     * @return The RegisterRange integer associated with the provided message
     * @throws IllegalArgumentException if message not associated with a register range
     */
    public static int registerRangeForMessage(Message message) {
        switch (message.type()) {
            case READ_INPUT_CONTACT:
                return INPUT_STATUS;
            case READ_OUTPUT_COIL:
                return COIL_STATUS;
            case READ_INPUT_REGISTER_INTEGER:
            case READ_INPUT_REGISTER_FLOAT:
            case READ_INPUT_REGISTER_STRING:
                return INPUT_REGISTER;
            case READ_HOLDING_REGISTER_INTEGER:
            case READ_HOLDING_REGISTER_FLOAT:
            case READ_HOLDING_REGISTER_STRING:
            case WRITE_HOLDING_REGISTER_INTEGER:
            case WRITE_HOLDING_REGISTER_FLOAT:
            case WRITE_HOLDING_REGISTER_STRING:
                return HOLDING_REGISTER;
        }
        throw new IllegalArgumentException("No register range associated with " + message.type());
    }
}
