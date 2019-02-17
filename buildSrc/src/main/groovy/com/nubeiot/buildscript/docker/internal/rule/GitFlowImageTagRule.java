package com.nubeiot.buildscript.docker.internal.rule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.Project;

import com.nubeiot.buildscript.ProjectUtils;

public class GitFlowImageTagRule extends ProjectImageTagRule {

    private static final String GIT_VERSION_TAG = "v\\d+\\.\\d+\\.\\d+(-.+)?";
    private static final String MASTER = "master";
    private final String branch;
    private final boolean latest;

    public GitFlowImageTagRule(Project project, String branch) {
        super(project);
        this.branch = branch;
        this.latest = Boolean.valueOf(ProjectUtils.extraProp(project, "docker.latest"));
    }

    @Override
    public String tag() {
        if (MASTER.equals(branch) || branch.matches(GIT_VERSION_TAG)) {
            return check(branch);
        }
        return check(branch + ".b" + ProjectUtils.extraProp(getProject(), "buildNumber"));
    }

    @Override
    public Set<String> images() {
        Set<String> images = new HashSet<>(super.images());
        if (latest && branch.matches(GIT_VERSION_TAG) && !getProject().getVersion().toString().endsWith("SNAPSHOT")) {
            images.add("latest");
        }
        return Collections.unmodifiableSet(images);
    }

}
