package com.nubeiot.edge.connector.modbus;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.edge.connector.modbus.json.Message;
import com.nubeiot.edge.connector.modbus.message.BooleanValue;
import com.nubeiot.edge.connector.modbus.message.FloatOperation;
import com.nubeiot.edge.connector.modbus.message.FloatValue;
import com.nubeiot.edge.connector.modbus.message.IntegerOperation;
import com.nubeiot.edge.connector.modbus.message.IntegerValue;
import com.nubeiot.edge.connector.modbus.message.ReadHoldingRegisterFloat;
import com.nubeiot.edge.connector.modbus.message.ReadHoldingRegisterInteger;
import com.nubeiot.edge.connector.modbus.message.ReadHoldingRegisterString;
import com.nubeiot.edge.connector.modbus.message.ReadInputContact;
import com.nubeiot.edge.connector.modbus.message.ReadInputRegisterFloat;
import com.nubeiot.edge.connector.modbus.message.ReadInputRegisterInteger;
import com.nubeiot.edge.connector.modbus.message.ReadInputRegisterString;
import com.nubeiot.edge.connector.modbus.message.ReadOutputCoil;
import com.nubeiot.edge.connector.modbus.message.StringOperation;
import com.nubeiot.edge.connector.modbus.message.StringValue;
import com.nubeiot.edge.connector.modbus.message.ValueOperation;
import com.nubeiot.edge.connector.modbus.message.WriteHoldingRegisterFloat;
import com.nubeiot.edge.connector.modbus.message.WriteHoldingRegisterInteger;
import com.nubeiot.edge.connector.modbus.message.WriteHoldingRegisterString;
import com.nubeiot.edge.connector.modbus.message.WriteOperation;
import com.nubeiot.edge.connector.modbus.message.WriteOutputCoil;
import com.nubeiot.edge.connector.modbus.message.*;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.serotonin.modbus4j.locator.NumericLocator;
import com.serotonin.modbus4j.locator.StringLocator;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.msg.ReadResponse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.nio.charset.Charset;
import java.util.function.ToIntFunction;

import static com.nubeiot.edge.connector.modbus.MessageHelpers.registerRangeForMessage;

public class ModbusVerticle extends AbstractVerticle {
    public static final String ADDRESS = "modbus";
    private final ModbusFactory factory = new ModbusFactory();
    private io.vertx.core.eventbus.Message<Object> request;
    private ModbusConnectionManager connections = new ModbusConnectionManager();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        if (!context.isWorkerContext()) {
            Exception exception = new IllegalStateException("ModbusVerticle must be deployed as a worker verticle. (single-threaded)");
            startFuture.fail(exception);
            throw exception;
        }
        vertx.eventBus().consumer(ADDRESS, this::handleRequest);
        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        connections.close();
        stopFuture.complete();
    }

    /**
     * Handles incoming vertx messages.
     *
     * @param request The vertx message to be processed
     */
    private void handleRequest(io.vertx.core.eventbus.Message<Object> request) {
        if (!(request.body() instanceof String)) return;
        this.request = request;
        System.out.println((String) this.request.body());
        Message message = Message.from((String) this.request.body());
        handleMessageBySubclass(message);
    }

    /**
     * The JSON of a Vert.x message is transformed into a {@link Message} instance. Depending on the {@link Message#type()}
     * the JSON is mapped to an appropriate subclass of the {@link Message} class.
     * <p>
     * This handler processes the {@link Message} further, based on its subclass, i.e. based on what message it is.
     *
     * @param message The message to be processed.
     */
    private void handleMessageBySubclass(Message message) {
        try {
            if (message instanceof ReadInputContact) {
                this.handleReadBooleanMessage((ReadInputContact) message);
                return;
            }
            if (message instanceof ReadOutputCoil) {
                this.handleReadBooleanMessage((ReadOutputCoil) message);
                return;
            }
            if (message instanceof ReadInputRegisterInteger) {
                this.handleIntegerReadMessage((ReadInputRegisterInteger) message);
                return;
            }
            if (message instanceof ReadInputRegisterFloat) {
                this.handleFloatReadMessage((ReadInputRegisterFloat) message);
                return;
            }
            if (message instanceof ReadHoldingRegisterInteger) {
                this.handleIntegerReadMessage((ReadHoldingRegisterInteger) message);
                return;
            }
            if (message instanceof ReadHoldingRegisterFloat) {
                this.handleFloatReadMessage((ReadHoldingRegisterFloat) message);
                return;
            }
            if (message instanceof ReadInputRegisterString) {
                this.handleStringReadMessage((ReadInputRegisterString) message);
                return;
            }
            if (message instanceof ReadHoldingRegisterString) {
                this.handleStringReadMessage((ReadHoldingRegisterString) message);
                return;
            }
            if (message instanceof WriteHoldingRegisterInteger) {
                this.handleIntegerWriteMessage((WriteHoldingRegisterInteger) message);
                return;
            }
            if (message instanceof WriteHoldingRegisterFloat) {
                this.handleFloatWriteMessage((WriteHoldingRegisterFloat) message);
                return;
            }
            if (message instanceof WriteOutputCoil) {
                this.handleBooleanWriteMessage((WriteOutputCoil) message);
                return;
            }
            if (message instanceof WriteHoldingRegisterString) {
                this.handleStringWriteMessage((WriteHoldingRegisterString) message);
                return;
            }
        } catch (ModbusTransportException | ErrorResponseException e) {
            replyWithFailure(e.getMessage());
            return;
        }
        throw new IllegalArgumentException("Trying to handle unknown message: " + message.toString());
    }

    private void handleReadBooleanMessage(ValueOperation message) throws ModbusTransportException {
        ReadResponse response = responseForReadMessage(message, 1);
        replyWithBoolean(response.getBooleanData()[0]);
    }

    private void handleIntegerReadMessage(IntegerOperation message) throws ModbusTransportException {
        ReadResponse response = responseForReadMessage(message, registersForByteCount(message.byteCount()));
        NumericLocator locator = numericLocatorForMessage(message, MessageHelpers::dataTypeForIntegerOperation);
        replyWithInteger(locator.bytesToValueRealOffset(response.getData(), 0).longValue());
    }

    private void handleFloatReadMessage(FloatOperation message) throws ModbusTransportException {
        ReadResponse response = responseForReadMessage(message, registersForByteCount(message.byteCount()));
        NumericLocator locator = numericLocatorForMessage(message, MessageHelpers::dataTypeForFloatOperation);
        replyWithFloat(locator.bytesToValueRealOffset(response.getData(), 0).floatValue());
    }

    private void handleStringReadMessage(StringOperation message) throws ModbusTransportException {
        ReadResponse response = responseForReadMessage(message, registersForByteCount(message.byteCount()));
        BaseLocator<String> locator = stringLocatorForMessage(message);
        replyWithString(locator.bytesToValueRealOffset(response.getData(), 0));
    }

    private int registersForByteCount(int byteCount) {
        return Math.max(1, byteCount / 2);
    }

    private void handleBooleanWriteMessage(WriteOutputCoil message) throws ErrorResponseException, ModbusTransportException {
        BaseLocator<Boolean> locator = booleanLocatorForMessage(message);
        handleWriteMessage(message, locator);
    }

    private void handleIntegerWriteMessage(WriteHoldingRegisterInteger message) throws ErrorResponseException, ModbusTransportException {
        NumericLocator locator = numericLocatorForMessage(message, MessageHelpers::dataTypeForIntegerOperation);
        handleWriteMessage(message, locator);
    }

    private void handleFloatWriteMessage(WriteHoldingRegisterFloat message) throws ErrorResponseException, ModbusTransportException {
        NumericLocator locator = numericLocatorForMessage(message, MessageHelpers::dataTypeForFloatOperation);
        handleWriteMessage(message, locator);
    }

    private void handleStringWriteMessage(WriteHoldingRegisterString message) throws ErrorResponseException, ModbusTransportException {
        BaseLocator<String> locator = stringLocatorForMessage(message);
        handleWriteMessage(message, locator);
    }

    private <T> void handleWriteMessage(WriteOperation<T> message, BaseLocator<? super T> locator) throws ErrorResponseException, ModbusTransportException {
        ModbusMaster master = connections.getMaster(message.connection());
        master.setValue(locator, message.value());
        request.reply(null);
    }

    private <T extends ValueOperation> NumericLocator numericLocatorForMessage(T message, ToIntFunction<T> messageToDataType) {
        return new NumericLocator(
                message.connection().slaveId(),
                registerRangeForMessage(message),
                message.offset(),
                messageToDataType.applyAsInt(message)
        );
    }

    private BaseLocator<String> stringLocatorForMessage(StringOperation message) {
        return new StringLocator(
                message.connection().slaveId(),
                registerRangeForMessage(message),
                message.offset(),
                DataType.VARCHAR,
                message.byteCount(),
                Charset.forName("ASCII")
        );
    }

    private BaseLocator<Boolean> booleanLocatorForMessage(ValueOperation message) {
        return BaseLocator.coilStatus(
                message.connection().slaveId(),
                message.offset()
        );
    }

    private ReadResponse responseForReadMessage(ValueOperation message, int byteCount) throws ModbusTransportException {
        ModbusMaster master = connections.getMaster(message.connection());
        ModbusRequest readRequest = factory.createReadRequest(
                message.connection().slaveId(),
                registerRangeForMessage(message),
                message.offset(),
                byteCount
        );
        ReadResponse response = (ReadResponse) master.send(readRequest);
        throwIfResponseIsException(response);
        return response;
    }

    private void throwIfResponseIsException(ModbusResponse response) throws ModbusTransportException {
        if (response.isException()) {
            throw new ModbusTransportException(response.getExceptionMessage());
        }
    }

    /**
     * Sends a boolean value back in response to the last received vertx request.
     *
     * @param value The boolean value to be packed and sent.
     */
    private void replyWithBoolean(boolean value) {
        request.reply(BooleanValue.of(value).toJson());
    }

    private void replyWithInteger(long value) {
        request.reply(IntegerValue.of(value).toJson());
    }

    private void replyWithFloat(double value) {
        request.reply(FloatValue.of(value).toJson());
    }

    private void replyWithString(String value) {
        request.reply(StringValue.of(value).toJson());
    }

    private void replyWithFailure(String message) {
        request.fail(500, message);
    }
}
