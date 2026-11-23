package com.github.maiflai

import org.gradle.api.Action
import org.gradle.api.tasks.testing.Test

class JacocoTestAction implements Action<Test> {

    private Object jacoco

    JacocoTestAction(Object jacoco) {
        this.jacoco = jacoco
    }

    @Override
    void execute(Test task) {
        if (jacoco && jacoco.enabled) {
            def jacocoJavaAgent = jacoco.getAsJvmArg()
            if (!task.allJvmArgs.contains(jacocoJavaAgent)) {
                task.jvmArgs jacocoJavaAgent
            }
        }
    }

}
