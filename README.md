gradle-scct
===========
A plugin to enable the use of scalatest in a gradle Scala project.

Getting started
---------------
```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.github.maiflai:gradle-scalatest:0.4'
    }
}

apply plugin: 'scalatest'

dependencies {
    compile 'org.scala-lang:scala-library:2.10.1'
    testCompile 'org.scalatest:scalatest_2.10:2.0'
    testRuntime 'org.pegdown:pegdown:1.1.0'
}
```

This replaces the existing test task actions with a scalatest implementation.

---
Note that the default behaviour is to use as many parallel threads as you have available processors.

`Test` tasks are modified at the time that you apply the plugin (as otherwise they would default to single-threaded).

To disable this, you should configure your test tasks accordingly.

```groovy
test {
    maxParallelForks = 1
}
```
