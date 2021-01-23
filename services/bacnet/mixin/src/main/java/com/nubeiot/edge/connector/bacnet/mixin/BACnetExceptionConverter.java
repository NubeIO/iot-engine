package com.nubeiot.edge.connector.bacnet.mixin;

import java.util.Objects;

import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.exceptions.EngineException;
import io.github.zero88.qwe.exceptions.converter.CarlExceptionConverter;

import com.nubeiot.edge.connector.bacnet.mixin.serializer.EncodableSerializer;
import com.serotonin.bacnet4j.exception.AbortAPDUException;
import com.serotonin.bacnet4j.exception.BACnetAbortException;
import com.serotonin.bacnet4j.exception.BACnetErrorException;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRejectException;
import com.serotonin.bacnet4j.exception.ErrorAPDUException;
import com.serotonin.bacnet4j.exception.RejectAPDUException;
import com.serotonin.bacnet4j.exception.SegmentedMessageAbortedException;
import com.serotonin.bacnet4j.type.Encodable;

import lombok.NonNull;

public final class BACnetExceptionConverter {

    public static CarlException convert(@NonNull BACnetException throwable) {
        Encodable reason = null;
        if (throwable instanceof AbortAPDUException) {
            reason = ((AbortAPDUException) throwable).getApdu().getAbortReason();
        }
        if (throwable instanceof BACnetAbortException) {
            reason = ((BACnetAbortException) throwable).getAbortReason();
        }
        if (throwable instanceof BACnetErrorException) {
            reason = ((BACnetErrorException) throwable).getBacnetError();
        }
        if (throwable instanceof BACnetRejectException) {
            reason = ((BACnetRejectException) throwable).getRejectReason();
        }
        if (throwable instanceof RejectAPDUException) {
            reason = ((RejectAPDUException) throwable).getApdu().getRejectReason();
        }
        if (throwable instanceof BACnetRejectException) {
            reason = ((BACnetRejectException) throwable).getRejectReason();
        }
        if (throwable instanceof ErrorAPDUException) {
            reason = ((ErrorAPDUException) throwable).getError();
        }
        if (throwable instanceof SegmentedMessageAbortedException) {
            reason = ((SegmentedMessageAbortedException) throwable).getAbort().getAbortReason();
        }
        if (Objects.isNull(reason)) {
            return CarlExceptionConverter.friendly(throwable);
        }
        final @NonNull Object encode = EncodableSerializer.encode(reason);
        return new EngineException(throwable);
    }

}
