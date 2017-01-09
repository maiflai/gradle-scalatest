gradle-scalatest
================
A plugin to enable the use of scalatest in a gradle Scala project. [![Build Status](https://travis-ci.org/maiflai/gradle-scalatest.svg?branch=master)](https://travis-ci.org/maiflai/gradle-scalatest)

Getting started
---------------
http://plugins.gradle.org/plugin/com.github.maiflai.scalatest

This replaces the existing test task actions with a scalatest implementation.

In addition to your `testCompile` dependency on scalatest, you also require a `testRuntime` dependency on pegdown in
order to create the HTML report.

```groovy
dependencies {
  testCompile 'org.scalatest:scalatest_2.11:3.0.1'
  testRuntime 'org.pegdown:pegdown:1.4.2'
}
```
---

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