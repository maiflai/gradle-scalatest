package com.github.maiflai

import org.gradle.api.Action
import org.gradle.api.tasks.testing.Test

class JacocoTestAction implements Action<Test> {

    @Override
    void execute(Test task) {
        // java.lang.InternalError: Malformed class name when finding by type
        def jacoco = task.extensions.findByName('jacoco')
        if (jacoco && jacoco.enabled) {
            def jacocoJavaAgent = jacoco.getAsJvmArg()
            if (!task.allJvmArgs.contains(jacocoJavaAgent)) {
                task.jvmArgs jacocoJavaAgent
            }
        }
    }

}
