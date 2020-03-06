package com.nubeiot.core.workflow;

import io.reactivex.annotations.Experimental;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;

@Experimental
public final class WorkflowType extends AbstractEnumType {

    public static final WorkflowType EVENTBUS = new WorkflowType("eventbus");

    private WorkflowType(String type) {
        super(type);
    }

    @JsonCreator
    public static WorkflowType factory(String type) {
        return EnumType.factory(type, WorkflowType.class);
    }

}
