package com.github.maiflai.scalatest

import org.gradle.api.Project

class ScalaTestExtension {

    private static String MEMENTO_KEY = 'gradle-scalatest.memento'
    private static String SINCE_KEY = 'gradle-scalatest.since'

    File memento
    Date since

    ScalaTestExtension(Project project) {

        String m = project.properties.get(MEMENTO_KEY)
        if (m != null) memento = project.file("$project.buildDir/scalatest/$m")

        String s = project.properties.get(SINCE_KEY)
        if (s != null) since = new Date(s.toLong())

    }

    def boolean isQuick() {
        memento != null || since != null
    }

    static def String toArgument(String memento) {
        "-P$MEMENTO_KEY=$memento"
    }

    static def String toArgument(Date since) {
        "-P$SINCE_KEY=$since.time"
    }
}
