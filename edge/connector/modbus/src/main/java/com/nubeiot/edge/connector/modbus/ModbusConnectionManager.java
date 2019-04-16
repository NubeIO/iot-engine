package com.nubeiot.edge.connector.modbus;

import com.nubeiot.edge.connector.modbus.message.Connection;
import com.nubeiot.edge.connector.modbus.serial.SerialConnection;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a pool of Modbus connection instances for re-use. Takes into account that only one master can operate on
 * a physical serial port at a time, and destroys old serial connections if a new one with changed connection parameters is
 * requested.
 * <p>
 * Devices are seldom, if ever, added or removed from the Modbus network, so it makes sense to reuse connection instances.
 */
public class ModbusConnectionManager {
    // we can reuse ModbusMaster instances, as long as keepAlive is set to false during creation
    private static final ModbusFactory factory = new ModbusFactory();
    private final Map<Connection, ModbusMaster> masterByConnection = new HashMap<>();

    /**
     * Returns a modbus master that can be used to send a Modbus package to a specific slave device.
     *
     * @param connection A description of the connection parameters for the target device.
     * @return A Modbus master that is targeted at the specified device.
     */
    public ModbusMaster getMaster(Connection connection) {
        ModbusMaster master = masterByConnection.computeIfAbsent(connection, this::createInitializedMasterForConnection);
        if (!master.isInitialized()) { // recompute if closed
            master = masterByConnection.compute(connection, (c, oldMaster) -> createInitializedMasterForConnection(c));
        }
        return master;
    }

    /**
     * Creates a modbus master that can be used to send a Modbus package to a specific slave device.
     *
     * @param connection A description of the connection parameters for the target device.
     * @return A Modbus master that is targeted at the specified device.
     */
    private ModbusMaster createInitializedMasterForConnection(Connection connection) {
        if (isSerial(connection)) {
            closeExistingMastersForDevice(connection.deviceName());
        }
        ModbusMaster master = createMasterByProtocol(connection);
        try {
            master.init();
            return master;
        } catch (ModbusInitException e) {
            throw new IllegalStateException("Unable to initialize master for connection: " + connection.toString(), e);
        }
    }

    private boolean isSerial(Connection connection) {
        switch (connection.protocol()) {
            case RTU:
                return true;
            case ASCII:
                return true;
            default:
                return false;
        }
    }

    private void closeExistingMastersForDevice(String deviceName) {
        for (Connection connection : masterByConnection.keySet()) {
            if (connection.deviceName().equals(deviceName)) {
                removeConnection(connection);
            }
        }
    }

    private void removeConnection(Connection connection) {
        masterByConnection.get(connection).destroy();
        masterByConnection.remove(connection);
    }

    private ModbusMaster createMasterByProtocol(Connection connection) {
        switch (connection.protocol()) {
            case TCP:
                return createTcpMasterFor(connection);
            case RTU:
                return createRtuMasterFor(connection);
            case ASCII:
                return createAsciiMasterFor(connection);
        }
        throw new IllegalArgumentException("Trying to create master for unsupported protocol: " + connection.protocol());
    }

    private ModbusMaster createTcpMasterFor(Connection connection) {
        IpParameters slaveAddress = new IpParameters();
        slaveAddress.setHost(connection.host());
        slaveAddress.setPort(connection.port());
        return factory.createTcpMaster(slaveAddress, false);
    }

    private ModbusMaster createRtuMasterFor(Connection connection) {
        SerialConnection serialConnection = new SerialConnection(connection);
        return factory.createRtuMaster(serialConnection);
    }

    private ModbusMaster createAsciiMasterFor(Connection connection) {
        SerialConnection serialConnection = new SerialConnection(connection);
        return factory.createAsciiMaster(serialConnection);
    }

    public void close() {
        for (ModbusMaster master : masterByConnection.values()) {
            master.destroy();
        }
        masterByConnection.clear();
    }
}
