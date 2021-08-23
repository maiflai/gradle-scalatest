package com.github.maiflai

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector

abstract class BaseExampleTest {

    protected File projectDir

    protected BaseExampleTest(String projectName) {
        projectDir = new File("src/test/examples/${projectName}")
    }

    protected void runTasks(String... tasks) {
        gradleConnector()
                .forTasks(tasks)
                .run()
    }

    protected void runTasks(ScalaTestPlugin.Mode mode, String... tasks) {
        gradleConnector()
                .addArguments("-P${ScalaTestPlugin.MODE}=$mode")
                .forTasks(tasks)
                .run()
    }

    private BuildLauncher gradleConnector() {
        GradleConnector
                .newConnector()
                .forProjectDirectory(projectDir)
                .connect()
                .newBuild()
                .setStandardOutput(System.out)
                .setStandardError(System.err)
                .addArguments('--console=plain', '--warning-mode=all')
    }

}
