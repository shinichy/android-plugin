---
layout: post
title: Annex B. Cookbook
subtitle: Commonly used rules and settings
---

This page lists a few configuration snippets we found useful when using
SBT-Android in our projects. Feel free to request additions to this list by
pull requests or the mailing list!

Unless specified, all code snippets are to be added to `build.sbt`.

# Building a regular (Ant) Java Project

Instead of having everything under `src/main` (like SBT or Gradle do), regular
Ant Java layouts put everything on the root of the project, and the sources in
`src/`.

```scala
androidJavaLayout
```

# Using Google repositories

These settings add some Google libraries (Play Services,...) to your project.

```scala
// Support library
libraryDependencies += "com.google.android" % "support-v4" % "r7"
```

```scala
// Google Play services
libraryDependencies += aarlib("com.google.android.gms" % "play-services" % "3.1.36")
```

# Using provided Google libraries

Libraries that are provided by Android (Maps,...) need to be specified with
`<uses-sdk>` in your manifest.

Then, use the corresponding setting keys to add them as provided dependencies :

```scala
// Google Maps
// TODO: Add a setting key to add them to unmanagedJars
```

# Using ActionBarSherlock

Add this to your configuration :

```scala
// ActionBarSherlock
libraryDependencies += apklib("com.actionbarsherlock" % "actionbarsherlock" % "4.3.1")
```

# Using Scaloid
