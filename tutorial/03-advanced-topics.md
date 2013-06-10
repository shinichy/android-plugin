---
layout: post
title: 3. Advanced topics
subtitle: On advanced build processes and interoperability
---

This section will talk in a little more detail about how to use SBT-Android for
more advanced purposes, as well as integration with existing tools (IntelliJ
IDEA, Gradle,...).

# Advanced build configuration

Up until now, we have used the `build.sbt` file to describe how to build our
apps. While this works well in practice for simple projects, for more complex
build situations you'll probably need something else.

You should note, first, that `build.sbt` files are essentially lines of Scala
code that are put together in one unique setting list.

`build.scala` files, on the contrary, let you build your own projects and
setting lists.

To create one, you first must import the required SBT components :

```scala
import sbt._
import Keys._
import Defaults._
```

Then, import the SBT-Android keys and helper methods :

```scala
import sbtandroid.AndroidPlugin._
```

Then, finally, create a `Build` object that will include your build settings :

```scala
object AndroidBuild extends Build {
  ...
}
```

Inside this object, you can put as many `Project`s as you like, describing
where your source files are, and what the relationships between them are.

As an example, here is how a standard Android project is defined :

```scala
object AndroidBuild extends Build {

  // Global settings
  val globalSettings = Seq(
    name := "Scratch",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.10.1",
    platformName := "android-16",
    keyalias := "change-me"
  )

  // Main project (equivalent to defining something in `build.sbt`)
  lazy val main = AndroidProject(
    "main",                     // Project name
    file("."),                  // Project base directory
    settings=globalSettings)    // Project settings

  lazy val tests = AndroidTestProject(
    "tests",                         // Project name
    file("tests"),                   // Project base directory
    settings=globalSettings)         // Project settings
    .dependsOn(main % "provided")    // Main is "provided"
    .settings(name := "MockaTests")  // Application name
}
```

Here, the `main` project is the equivalent to what you've defined previously in
the `build.sbt` file.

On the other hand, `tests` is a "testing" project, that depends on code from
`main` at runtime (`main`'s classpath will be loaded by Dalvik when running the
Android instrumentation framework), but must not be included in the APK.

We use a few methods that may need explanation :

  * `AndroidProject` and `AndroidTestProject` are two helper methods letting you
    create Android projects more easily. Standard Java or Scala projects use the
    original SBT `Project` method, as in `val main = Project("main", file(."))`.

  * `dependsOn` is a method of a `Project` that defines dependencies on other projects.

  * `settings` is a method of a `Project` object that defines additional settings.

# Using the NDK

For now, the NDK (Native Development Kit) settings are not included by default, as most people probably
won't use it.

To start using it, just add the `androidNdk` setting key to your `build.sbt`
file or `build.scala` project.

Then, set the `jniClasses` key to include the classes that have JNI methods, such as :

```scala
jniClasses += "com.scratch.MyClassWithNative"
```

By default, SBT-Android generates one header per Java class. You can set the
path to a single generated header file with the `javahOutputFile` key :

```scala
javahOutputFile := Some(new File("native.h"))
```

The make environment variable `SBT_MANAGED_JNI_INCLUDE` can be used to refer to
the directory containing the generated header files :

```makefile
# Android.mk
LOCAL_C_INCLUDES += \
        ... \
        $(SBT_MANAGED_JNI_INCLUDE)
```

# IntelliJ integration

[SBT-Idea](https://github.com/mpeltonen/sbt-idea) can be used to generate
Android IntelliJ projects from SBT.

Just run the `gen-idea` command to generate an IntelliJ project :

```bash
> sbt gen-idea                                                                                                                                          master [f5a30a9] modified
[info] Loading project definition from .../scratch/project
[info] Set current project to Scratch (in build file:.../scratch/)
[info] Creating IDEA module for project 'Scratch' ...
...
[info] Created .../scratch/.idea_modules/Scratch.iml
[info] Created .../scratch/.idea_modules/Scratch-build.iml
```

Then, start IntelliJ and open the project :

![IntelliJ Welcome Screen](images/IntelliJ-OpenProject.png)
![Choose project](images/IntelliJ-ChooseProject.png)

Let IntelliJ read and index everything, and you'll be able to open the main
class :

![Main IntelliJ view](images/IntelliJ-OpenedProject.png)

Now, to start your application, use the _Run_ menu :

![IntelliJ Run Menu](images/IntelliJ-Run.png)

Create a new run configuration :

![IntelliJ New configuration](images/IntelliJ-Configuration.png)

I generally use the "Chooser dialog" to select the device I want my project to
run on, because IntelliJ has the habit of starting an emulator by default,
which I don't want :

![IntelliJ Edit Configuration](images/IntelliJ-RunConfig.png)

Finally, click "Run" and you're good to go. In the future, just use the "Run"
and "Debug" icons on IntelliJ's toolbar :

![IntelliJ Play](images/IntelliJ-Play.png)

_**Note:** The very first time you use a SBT-generated project, you might have to
configure the Android target. It has to be called something like **Android
4.1.2 Platform** (change the version to suit your needs) and will look like this :_

![IntelliJ Target](images/IntelliJ-AndroidPlatform.png)

# Eclipse integration

_(Upcoming)_

# Gradle integration

_(Upcoming)_
