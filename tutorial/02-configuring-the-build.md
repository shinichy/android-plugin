---
layout: post
title: 2. Configuring the build
subtitle: Common SBT-Android features
---

This section will explain the SBT-Android build process into a little more
detail.

# Starting SBT

SBT is the build tool our plugin runs on. It is used by the Scala community and
can be used to replace tools like Maven or Gradle.

People generally use SBT through its command prompt, started by running `sbt`
in a terminal. In there, you can inspect the build settings and run tasks and
commands.

Before starting, SBT first checks for either `build.sbt` (which is the simplest
way of starting a SBT project), or a `project/build.scala` file (which can be
used to configure more complex builds). Remember that we have already created a
`build.sbt` file in section 1!

SBT-Android is a SBT _plugin_, and as such is set up in `project/plugins.sbt`
by adding a line that looks like this :

```scala
addSbtPlugin("org.scala-sbt" % "sbt-android-plugin" % "0.7-SNAPSHOT")
```

_**Note:** Plugins are essentially additional classes loaded by SBT on the
project classpath. Here we use a Maven-style reference : `org.scala-sbt` is the
artifact organization, `sbt-android-plugin` is the artifact name, and
`0.7-SNAPSHOT` is the version._

# Using SBT

Within the SBT prompt, you can :

  * Run a task, such as the `start` task we saw earlier, or `apk`, which will
    generate an APK package.

  * Show the values returned by settings and tasks. For example, running `show
    use-proguard` will show `true` or `false`, depending on whether Proguard is
    enabled for your build.

  * Change settings and tasks on the fly.

SBT will place all the build artifacts and temporary files somewhere under the
`target/` directory.

# General build process

The SBT-Android build process is very similar to a standard Android build :

  * It first runs _source generators_ such as the `aapt` and `aidl` commands to parse resources and
    generate, among others, `R.java` files.

    Generated sources end up

  * 

# Controlling Proguard

# Using libraries and dependencies
