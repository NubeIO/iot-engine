package com.nubeiot.buildscript.docker

import org.junit.Assert
import org.junit.Test

class ArchTest {

    @Test
    void canAddTaskToProject() {
        Assert.assertEquals("amd64", Arch.LINUX64.canonicalName)
    }

}
