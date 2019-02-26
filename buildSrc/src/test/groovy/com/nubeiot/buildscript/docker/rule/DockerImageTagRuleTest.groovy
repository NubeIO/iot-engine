package com.nubeiot.buildscript.docker.rule

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import com.nubeiot.buildscript.docker.internal.Arch
import com.nubeiot.buildscript.docker.internal.OperatingSystem
import com.nubeiot.buildscript.docker.internal.rule.ArchTagRule
import com.nubeiot.buildscript.docker.internal.rule.CustomImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.DockerImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.GCRImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.GitFlowImageTagRule
import com.nubeiot.buildscript.docker.internal.rule.ProjectImageTagRule

class DockerImageTagRuleTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().withName("test").build()
        project.version = "1.0.0"
        project.ext.buildNumber = "10"
    }

    @Test
    void test_rule() {
        def rule = new ProjectImageTagRule(project)
        Assert.assertEquals("test", rule.repository())
        Assert.assertEquals("1.0.0.b10", rule.tag())
        Assert.assertEquals(["test:1.0.0.b10"] as Set, rule.images())
    }

    @Test
    void test_parse() {
        def rule = DockerImageTagRule.parse("test:1.0.0.b10")
        Assert.assertEquals("test", rule.repository())
        Assert.assertEquals("1.0.0.b10", rule.tag())
        Assert.assertEquals(["test:1.0.0.b10"] as Set, rule.images())
    }

    @Test
    void test_gitflow_rule() {
        def rule = new GitFlowImageTagRule(project, "feature/docker")
        Assert.assertEquals("test", rule.repository())
        Assert.assertEquals("feature-docker.b10", rule.tag())
        Assert.assertEquals(["test:feature-docker.b10"] as Set, rule.images())
    }

    @Test
    void test_gitflow_master() {
        def rule = new GitFlowImageTagRule(project, "master")
        Assert.assertEquals("test", rule.repository())
        Assert.assertEquals("master", rule.tag())
        Assert.assertEquals(["test:master"] as Set, rule.images())
    }

    @Test
    void test_gitflow_release() {
        project.ext["docker.latest"] = true
        def rule = new GitFlowImageTagRule(project, "v1.0.0-alpha")
        Assert.assertEquals("test", rule.repository())
        Assert.assertEquals("v1.0.0-alpha", rule.tag())
        Assert.assertEquals(["test:v1.0.0-alpha", "latest"] as Set, rule.images())
    }

    @Test
    void test_custom_wrap() {
        def rule = new CustomImageTagRule(project, "tag1 tag2")
        Assert.assertEquals("test", rule.repository())
        Assert.assertEquals("1.0.0.b10", rule.tag())
        Assert.assertEquals(["test:tag1", "test:tag2"] as Set, rule.images())
    }

    @Test
    void test_arch_rule_not_default() {
        def rule = new ArchTagRule(new ProjectImageTagRule(project), Arch.LINUX64, OperatingSystem.DEBIAN,
                                   OperatingSystem.ALPINE)
        Assert.assertEquals("amd64/test", rule.repository())
        Assert.assertEquals("1.0.0.b10-debian", rule.tag())
        Assert.assertEquals(["amd64/test:1.0.0.b10-debian"] as Set, rule.images())
    }

    @Test
    void test_arch_rule_default() {
        def rule = new ArchTagRule(new ProjectImageTagRule(project), Arch.LINUX64, OperatingSystem.DEBIAN,
                                   OperatingSystem.DEBIAN)
        Assert.assertEquals("amd64/test", rule.repository())
        Assert.assertEquals("1.0.0.b10", rule.tag())
        Assert.assertEquals(["amd64/test:1.0.0.b10"] as Set, rule.images())
    }

    @Test
    void test_arch_rule_from_custom() {
        def rule = new ArchTagRule(new CustomImageTagRule(project, "tag1 tag2"), Arch.LINUX64,
                                   OperatingSystem.DEBIAN, OperatingSystem.DEBIAN)
        Assert.assertEquals("amd64/test", rule.repository())
        Assert.assertEquals("1.0.0.b10", rule.tag())
        Assert.assertEquals(["amd64/test:tag1", "amd64/test:tag2"] as Set, rule.images())
    }

    @Test
    void test_gcr_rule() {
        def rule = new GCRImageTagRule(new ProjectImageTagRule(project), "gcr.io", "projectId")
        Assert.assertEquals("gcr.io/projectId/test", rule.repository())
        Assert.assertEquals("1.0.0.b10", rule.tag())
        Assert.assertEquals(["gcr.io/projectId/test:1.0.0.b10"] as Set, rule.images())
    }

    @Test(expected = IllegalArgumentException.class)
    void test_gcr_rule_invalid_host() {
        new GCRImageTagRule(new ProjectImageTagRule(project), "abc.io", "projectId")
    }


    @Test
    void test_gcr_rule_from_arch_rule() {
        def rule = new GCRImageTagRule(new ArchTagRule(new ProjectImageTagRule(project), Arch.LINUX64, OperatingSystem.DEBIAN,
                                                       OperatingSystem.DEBIAN), "gcr.io", "projectId")
        Assert.assertEquals("gcr.io/projectId/amd64/test", rule.repository())
        Assert.assertEquals("1.0.0.b10", rule.tag())
        Assert.assertEquals(["gcr.io/projectId/amd64/test:1.0.0.b10"] as Set, rule.images())
    }
}
