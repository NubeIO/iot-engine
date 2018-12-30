package com.nubeiot.core.statemachine;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.StateException;

public class StateMachineTest {

    @BeforeClass
    public static void init() {
        StateMachine.init();
    }

    @Test(expected = StateException.class)
    public void test_Halt_Conflict() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.HALT, "module");
    }

    @Test
    public void test_Halt_From_StateEnable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.HALT, "module");
    }

    @Test
    public void test_validate_NonExist() {
        StateMachine.instance().validate(null, EventAction.CREATE, "service");
    }

    @Test(expected = AlreadyExistException.class)
    public void test_validate_NonExist_Conflict() {
        StateMachine.instance().validate("", EventAction.CREATE, "service");
    }

    @Test
    public void test_validate_Exist() {
        StateMachine.instance().validate("hello", EventAction.UPDATE, "service");
    }

    @Test(expected = NotFoundException.class)
    public void test_validate_Exist_Conflict() {
        StateMachine.instance().validate(null, EventAction.UPDATE, "service");
    }

}