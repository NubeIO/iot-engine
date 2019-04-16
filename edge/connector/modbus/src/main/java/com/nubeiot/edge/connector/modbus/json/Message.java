package com.nubeiot.edge.connector.modbus.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubeiot.edge.connector.modbus.message.Type;
import com.nubeiot.edge.connector.modbus.message.TypedMessage;
import com.nubeiot.edge.connector.modbus.message.*;

public abstract interface Message {
    Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(MessageAdapterFactory.create())
            .create();

    /**
     * Convert a JSON {@link String} representation of a {@link Message} back to an instance of the appropriate
     * {@link Message} subclass.
     * <p>
     * The correct Message subclass is automatically detected based on the <code>type</code> property of the JSON.
     * <p>
     * The {@link Message} will be returned as the detected sub-class of the {@link Message} class. Due to Java limitations,
     * the {@link Message} instance has to be manually cast to the specific subclass after it is returned.
     *
     * <p>
     * <pre>
     * {@code
     * Message response = Message.from(json);
     * if (response instanceof BooleanValue) ...
     * }
     * </pre>
     *
     * If the expected subclass is known, it can instead be provided as the second argument, so that type checking
     * and casting are no longer required.
     *
     * @param json The JSON representation of a {@link Message} subclass
     * @return The instance of the {@link Message} subclass, based on the provided JSON
     */
    static Message from(String json) {
        Class<? extends Message> messageClass = gson.fromJson(json, TypedMessage.class).type().messageClass;
        return gson.fromJson(json, messageClass);
    }

    /**
     * Transform a JSON {@link String} representation of a {@link Message} back to an specific {@link Message} subclass
     * instance.
     *
     * @param json         The JSON representation of a {@link Message} subclass
     * @param messageClass The {@link Message} subclass to which the provided JSON will be mapped
     * @return The instance of the {@link Message} subclass, based on the provided JSON
     */
    static <T extends Message> T from(String json, Class<T> messageClass) {
        return gson.fromJson(json, messageClass);
    }

    /**
     * Format this message as a JSON {@link String}, suitable for sending over the Vert.x Event Bus.
     *
     * @return This instance as JSON {@link String}.
     */
    default String toJson() {
        return gson.toJson(this);
    }

    /**
     * Used to identify the subclass of this {@link Message} when parsing from JSON.
     *
     * @return The type of this {@link Message}, so that it can be mapped to a subclass.
     */
    Type type();

}
