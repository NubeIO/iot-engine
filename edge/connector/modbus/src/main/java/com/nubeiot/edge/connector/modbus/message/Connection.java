package com.nubeiot.edge.connector.modbus.message;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;


@AutoValue
public abstract class Connection {
    public static final int DEFAULT_BOUD_RATE = 9600;
    public static final Parity DEFAULT_PARITY = Parity.NONE;
    public static final int DEFAULT_BITS_PER_BYTE = 8;
    public static final int DEFAULT_STOP_BIT_COUNT = 1;

    public enum Protocol {
        TCP,
        RTU,
        ASCII,
    }

    public enum Parity {
        NONE,
        ODD,
        EVEN,
    }

    public abstract Protocol protocol();

    public abstract Integer slaveId();

    @Nullable
    public abstract String host();

    @Nullable
    public abstract Integer port();

    @Nullable
    public abstract String deviceName();

    @Nullable
    public abstract Integer baudRate();

    @Nullable
    public abstract Parity parityBit();

    @Nullable
    public abstract Integer bitsPerByte();

    @Nullable
    public abstract Integer stopBitCount();

    public static Builder builder() {
        return new AutoValue_Connection.Builder()
                .setBaudRate(DEFAULT_BOUD_RATE)
                .setParityBit(DEFAULT_PARITY)
                .setBitsPerByte(DEFAULT_BITS_PER_BYTE)
                .setStopBitCount(DEFAULT_STOP_BIT_COUNT);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setProtocol(Protocol value);

        public abstract Builder setSlaveId(Integer slaveId);

        public abstract Builder setHost(String value);

        public abstract Builder setPort(Integer port);

        public abstract Builder setDeviceName(String value);

        public abstract Builder setBaudRate(Integer value);

        public abstract Builder setParityBit(Parity value);

        public abstract Builder setBitsPerByte(Integer value);

        public abstract Builder setStopBitCount(Integer value);

        abstract Protocol protocol();

        abstract String host();

        abstract Integer port();

        abstract String deviceName();

        abstract Integer baudRate();

        abstract Parity parityBit();

        abstract Integer bitsPerByte();

        abstract Integer stopBitCount();

        abstract Connection autoBuild(); // package-private

        public Connection build() {
            // protocol and slaveId validation is handled by AutoValue
            if (protocol() == Protocol.TCP) {
                setDeviceName(null);
                setBaudRate(null);
                setParityBit(null);
                setBitsPerByte(null);
                setStopBitCount(null);
                Objects.requireNonNull(host());
                Objects.requireNonNull(port());
            }
            if (protocol() == Protocol.RTU || protocol() == Protocol.ASCII) {
                setHost(null);
                setPort(null);
                Objects.requireNonNull(deviceName());
                Objects.requireNonNull(baudRate());
                Objects.requireNonNull(parityBit());
                Objects.requireNonNull(bitsPerByte());
                Objects.requireNonNull(stopBitCount());
                if (stopBitCount() < 1 || stopBitCount() > 2) {
                    throw new IllegalStateException("Illegal number of stop bits: " + stopBitCount());
                }
                if (protocol() == Protocol.RTU && bitsPerByte() != 8) {
                    throw new IllegalStateException("RTU only supports 8 bits per byte. Received: " + bitsPerByte());
                }
                if (protocol() == Protocol.ASCII && (bitsPerByte() < 7 || bitsPerByte() > 8)) {
                    throw new IllegalStateException("ASCII supports 7 or 8 bits per byte. Received: " + bitsPerByte());
                }
            }

            return autoBuild();
        }
    }

    public static TypeAdapter<Connection> typeAdapter(Gson gson) {
        return new AutoValue_Connection.GsonTypeAdapter(gson);
    }
}
