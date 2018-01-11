package com.github.maiflai

import groovy.transform.Immutable
import org.gradle.api.internal.file.FileResolver
import org.gradle.process.internal.DefaultExecActionFactory
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction
import org.gradle.util.GradleVersion

@Immutable
class BackwardsCompatibleJavaExecActionFactory {
    String gradleVersion

    JavaExecAction create(FileResolver fileResolver) {
        boolean hasExecFactory = GradleVersion.version(gradleVersion) > GradleVersion.version("4.4.1")
        if (hasExecFactory) {
            new DefaultExecActionFactory(fileResolver).newJavaExecAction()
        } else {
            new DefaultJavaExecAction(fileResolver)
        }
    }
}
