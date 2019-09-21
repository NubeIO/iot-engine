package com.nubeiot.core.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UUID64 {

    public static String random() {
        return uuidToBase64(UUID.randomUUID());
    }

    public static String uuidToBase64(String uuidStr) {
        return uuidToBase64(UUID.fromString(Strings.requireNotBlank(uuidStr)));
    }

    public static String uuidToBase64(@NonNull UUID uuid) {
        byte[] bytes = uuidToBytes(uuid);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String uuid64ToUuidStr(String uuid64) {
        return uuid64ToUuid(uuid64).toString();
    }

    public static UUID uuid64ToUuid(String uuid64) {
        final String src = Strings.requireNotBlank(uuid64);
        if (src.length() == 36) {
            return UUID.fromString(src);
        }
        if (src.length() != 22) {
            throw new IllegalArgumentException("Invalid uuid");
        }
        byte[] decoded = Base64.getUrlDecoder().decode(src);
        return uuidFromBytes(decoded);
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID uuidFromBytes(byte[] decoded) {
        ByteBuffer bb = ByteBuffer.wrap(decoded);
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

}
