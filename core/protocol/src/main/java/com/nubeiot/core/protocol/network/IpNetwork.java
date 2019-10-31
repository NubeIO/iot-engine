package com.nubeiot.core.protocol.network;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(value = AccessLevel.PROTECTED)
@Accessors(chain = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class IpNetwork implements Ethernet {

    private final int index;
    @EqualsAndHashCode.Include
    private final String name;
    private final String displayName;
    @EqualsAndHashCode.Include
    private final String macAddress;
    @EqualsAndHashCode.Include
    private final String cidrAddress;
    private String hostAddress;

    abstract int version();

    @Override
    public final @NonNull String type() {
        return "ipv" + version();
    }

    @Getter(value = AccessLevel.PROTECTED)
    @Accessors(fluent = true)
    @JsonPOJOBuilder(withPrefix = "")
    @SuppressWarnings("unchecked")
    static abstract class IpBuilder<T extends IpNetwork, B extends IpBuilder> {

        private int index;
        private String name;
        private String displayName;
        private String macAddress;
        private String cidrAddress;
        private String hostAddress;

        public abstract T build();

        public B index(int index) {
            this.index = index;
            return (B) this;
        }

        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        public B displayName(String displayName) {
            this.displayName = displayName;
            return (B) this;
        }

        public B macAddress(String macAddress) {
            this.macAddress = macAddress;
            return (B) this;
        }

        public B cidrAddress(String cidrAddress) {
            this.cidrAddress = cidrAddress;
            return (B) this;
        }

        public B hostAddress(String hostAddress) {
            this.hostAddress = hostAddress;
            return (B) this;
        }

    }

}
