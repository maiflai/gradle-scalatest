gradle-scalatest
================
A plugin to enable the use of scalatest in a gradle Scala project. [![Java CI with Gradle](https://github.com/maiflai/gradle-scalatest/actions/workflows/gradle.yml/badge.svg)](https://github.com/maiflai/gradle-scalatest/actions/workflows/gradle.yml)

Getting started
---------------
http://plugins.gradle.org/plugin/com.github.maiflai.scalatest

This replaces the existing test task actions with a scalatest implementation (see [Other Frameworks](#other-frameworks) below).

In addition to your `testCompile` dependency on scalatest, you also require a `testRuntime` dependency on pegdown in
order to create the HTML report.

```groovy
dependencies {
  testCompile 'org.scalatest:scalatest_2.13:3.2.0'
  testRuntime 'com.vladsch.flexmark:flexmark-all:0.35.10'
  // note that older versions of scalatest have a testRuntime dependency on pegdown to produce HTML reports.
}
```
---

Compatibility
-------------
This plugin aims to be compatible with the current version of Gradle. 
The table below indicates the minimum required version.

|Gradle|gradle-scalatest|scalatest|
|------|----------------|---------|
|7.0   |0.31            |2.0      |
|6.5.1 |0.25            |2.0      |
|5.3   |0.25            |2.0      |
|5.2.1 |0.24            |2.0      |
|5.0   |0.23            |2.0      |
|4.5   |0.19            |2.0      |
|4.0   |0.16            |2.0      |
|3.0   |0.14            |2.0      |
|2.14.1|0.13            |2.0      |

Parallel Testing
----------------
The default behaviour is to use as many parallel threads as you have available processors.

`Test` tasks are modified at the time that you apply the plugin (as otherwise they would default to single-threaded).

To disable this, you should configure your test tasks accordingly.

```groovy
test {
    maxParallelForks = 1
}
```

Tags
----
Scalatest provides support for filtering tests by tagging. We cannot use the `PatternSet` provided by the `Test`
task because it applies this filter to test files internally.

We therefore provide an extension named `tags` to `Test` tasks.

```groovy
test {
    tags {
        exclude 'org.scalatest.tags.Slow'
    }
}

task slowTest(type: Test) {
    tags {
        include 'org.scalatest.tags.Slow'
    }
}
```

Suites
------
Suites are supported with another extension to the `Test` task.
```groovy
task userStories(type: Test) {
    suite 'com.example.UserStories'
    // suites 'a.Spec', 'b.Spec', 'etc'
}
```

Filtering
---------
Scalatest provides a simplified wildcard syntax for selecting tests. 
We directly map [Gradle test filters](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/testing/TestFilter.html) to this form.

```groovy
test {
    filter {
        includeTestsMatching 'MyTest'
    }
}
```

This can also be supplied on the command line:

```
./gradlew test --tests MyTest
```

ConfigMap
---------
Additional configuration can be passed to Scalatest using the [config map](http://www.scalatest.org/user_guide/using_the_runner#configMapSection)

```groovy 
test {
    config 'db.name', 'testdb'
}
```

```groovy 
test {
    configMap([
        'db.name': 'testdb'
        'server': '192.168.1.188'
        ])
}
```

Custom Reporters
----------------
```groovy
test {
    reporter 'my.Reporter'
}
```

Other Frameworks
----------------
The default behaviour is to replace all `Test` tasks with a scalatest implementation.

This may not be appropriate if you are migrating an existing project to scalatest.

The `com.github.maiflai.gradle-scalatest.mode` property may be configured to support the following behaviour:

|Value        |Behaviour                                              |
|-------------|-------------------------------------------------------|
|replaceAll   |replace all instances of the `Test` task               |
|replaceOne   |replace only the `Test` task named "test"              |
|append       |create a new scalatest `Test` task named "scalatest"   |

It's probably easiest to set this in a gradle.properties file at the root of your project.

```
com.github.maiflai.gradle-scalatest.mode = append
```

If you then want to use scalatest to run other `Test` tasks, you can instruct this plugin to configure those tasks.

```
task myTest(dependsOn: testClasses, type: Test, group: 'verification') {
    com.github.maiflai.ScalaTestPlugin.configure(it)
    tags {
        include 'com.example.tags.MyTag'
    }
}
```