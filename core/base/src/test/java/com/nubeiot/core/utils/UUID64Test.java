package com.nubeiot.core.utils;

import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class UUID64Test {

    @Test
    public void encode_decode() {
        final String uuidStr = "33c4e146-78ce-4bf7-b9bb-75c222dd00b0";
        final String uuid64 = "M8ThRnjOS_e5u3XCIt0AsA";
        Assert.assertEquals(uuid64, UUID64.uuidToBase64(uuidStr));
        Assert.assertEquals(22, uuid64.length());
        Assert.assertEquals(uuidStr, UUID64.uuid64ToUuidStr(uuid64));
    }

    @Test
    public void each_encode_decode() {
        IntStream.range(0, 22).forEach(i -> Assert.assertEquals(22, UUID64.uuidToBase64(UUID.randomUUID()).length()));
    }

    @Test
    public void decode_from_uuid() {
        final String uuid64 = "33c4e146-78ce-4bf7-b9bb-75c222dd0088";
        Assert.assertEquals(UUID.fromString(uuid64), UUID64.uuid64ToUuid(uuid64));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decode_failed() {
        UUID64.uuid64ToUuidStr("xuz01234abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void decode_invalid_char() {
        UUID64.uuid64ToUuidStr("$aab$aab$aab$aab$aab$a");
    }

}
