package com.nubeiot.core.statemachine;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.StateException;

public class StateMachineTest {

    @BeforeClass
    public static void init() {
        StateMachine.init();
    }

    @Test(expected = StateException.class)
    public void test_Halt_From_Disable_To_Disable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.HALT, "module", State.DISABLED);
    }

    @Test(expected = StateException.class)
    public void test_Halt_From_Enable_To_Enable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.HALT, "module", State.ENABLED);
    }

    @Test(expected = StateException.class)
    public void test_Halt_From_Disable_To_Enable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.HALT, "module", State.ENABLED);
    }

    @Test
    public void test_Halt_From_Enable_To_Disable_State() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.HALT, "module", State.DISABLED);
    }

    @Test
    public void test_Patch_From_Enable_To_Enable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.PATCH, "module", State.ENABLED);
    }

    @Test
    public void test_Patch_From_Disable_To_Enable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.PATCH, "module", State.ENABLED);
    }

    @Test
    public void test_Patch_From_Disable_To_Disable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.PATCH, "module", State.DISABLED);
    }

    @Test
    public void test_Patch_From_Enable_To_Disable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.PATCH, "module", State.DISABLED);
    }

    @Test
    public void test_Migrate_From_Pending_To_Enable() {
        StateMachine.instance().validateConflict(State.PENDING, EventAction.MIGRATE, "module", State.ENABLED);
    }

    @Test
    public void test_Migrate_From_Enable_To_Enable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.MIGRATE, "module", State.ENABLED);
    }

    @Test
    public void test_Migrate_From_Disable_To_Enable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.MIGRATE, "module", State.ENABLED);
    }

    @Test
    public void test_Migrate_From_Disable_To_Disable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.MIGRATE, "module", State.DISABLED);
    }

    @Test
    public void test_Migrate_From_Enable_To_Disable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.MIGRATE, "module", State.DISABLED);
    }

    @Test
    public void test_Update_From_Enable_To_Enable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.UPDATE, "module", State.ENABLED);
    }

    @Test
    public void test_Update_From_Disable_To_Enable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.UPDATE, "module", State.ENABLED);
    }

    @Test
    public void test_Update_From_Disable_To_Disable() {
        StateMachine.instance().validateConflict(State.DISABLED, EventAction.UPDATE, "module", State.DISABLED);
    }

    @Test
    public void test_Update_From_Enable_To_Disable() {
        StateMachine.instance().validateConflict(State.ENABLED, EventAction.UPDATE, "module", State.DISABLED);
    }

    @Test
    public void test_Init_From_None_To_Enabled() {
        StateMachine.instance().validateConflict(State.NONE, EventAction.INIT, "module", State.ENABLED);
    }

    @Test
    public void test_Create_From_null_To_Enabled() {
        StateMachine.instance().validateConflict(null, EventAction.CREATE, "module", State.ENABLED);
    }

    @Test
    public void test_validate_NonExist_Init() {
        StateMachine.instance().validate(null, EventAction.INIT, "service");
    }

    @Test
    public void test_validate_Exist_Init() {
        StateMachine.instance().validate("", EventAction.INIT, "service");
    }

    @Test
    public void test_validate_NonExist_Create() {
        StateMachine.instance().validate(null, EventAction.CREATE, "service");
    }

    @Test
    public void test_validate_Exist_Create() {
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
