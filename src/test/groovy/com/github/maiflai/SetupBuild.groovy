package com.github.maiflai

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector

/**
 * Common utilities for setting up a build for testing.
 *
 * @author scr on 5/18/17.
 */
class SetupBuild {
    static BuildLauncher setupBuild(File projectRoot) {
        return GradleConnector.
                newConnector().
                forProjectDirectory(projectRoot).
                connect().
                newBuild()
    }
}
