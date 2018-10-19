package com.nubeio.iot.share.statemachine;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nubeio.iot.share.enums.State;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.exceptions.StateException;

public class StateMachineTest {

    @BeforeClass
    public static void init() {
        StateMachine.init();
    }

    @Test(expected = StateException.class)
    public void testHalt() {
        StateMachine.instance().validateConflict(State.DISABLED, EventType.HALT, "module");
    }

    @Test
    public void testHalt_FromEnable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventType.HALT, "module");
    }

}