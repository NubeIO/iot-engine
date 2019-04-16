package com.nubeiot.edge.connector.modbus.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.nubeiot.edge.connector.modbus.message.Connection;
import com.serotonin.modbus4j.serial.SerialPortWrapper;

import java.io.InputStream;
import java.io.OutputStream;

import static com.fazecast.jSerialComm.SerialPort.*;

public class SerialConnection implements SerialPortWrapper {
    private final SerialPort port;

    public SerialConnection(Connection connection) {
        port = SerialPort.getCommPort(connection.deviceName());
        port.setBaudRate(connection.baudRate());
        port.setParity(parityToNumber(connection.parityBit()));
        port.setNumDataBits(connection.bitsPerByte());
        port.setNumStopBits(connection.stopBitCount());
    }
    private int parityToNumber(Connection.Parity parity) {
        switch(parity) {
            case NONE:
                return NO_PARITY;
            case EVEN:
                return EVEN_PARITY;
            case ODD:
                return ODD_PARITY;
        }
        return 0;
    }
    @Override
    public void close() {
        port.closePort();
    }

    @Override
    public void open() {
        port.openPort();
    }

    @Override
    public InputStream getInputStream() {
        return port.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return port.getOutputStream();
    }

    @Override
    public int getBaudRate() {
        return port.getBaudRate();
    }

    @Override
    public int getFlowControlIn() {
        return 0;
    }

    @Override
    public int getFlowControlOut() {
        return 0;
    }

    @Override
    public int getDataBits() {
        return port.getNumDataBits();
    }

    @Override
    public int getStopBits() {
        return port.getNumStopBits();
    }

    @Override
    public int getParity() {
        return port.getParity();
    }
}
