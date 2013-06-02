---
layout: post
title: 1. Getting Started
subtitle: Creating a simple project from scratch
---

**Welcome to SBT-Android!**

The following tutorial is aimed at people who want to discover Scala on
Android. Since Android apps rely heavily on Java, other languages targetting
the Java Virtual Machine can also be used on Android, and among them is Scala.

[Scala](http://www.scala-lang.org) is a functional, strongly-typed language that brings quite a number of
handy features for app development, and this tutorial will try to list a few of
them.

Scala is quite close to Java, and you can freely mix Java and Scala code if you
wish. You don't really need to know Scala or SBT ([_Simple Build Tool_](http://www.scala-sbt.org), which we
use to build Scala apps) to follow this tutorial, so we'll try to explain the
most commonly-used commands and features.

If you find something unclear or lacking, the whole tutorial is written in
Markdown and [hosted on
GitHub](https://github.com/fxthomas/android-plugin/tree/gh-pages/). Don't
hesitate to send pull requests there!

If you have trouble, most of us hang out in
[scala-on-android](http://groups.google.com/group/scala-on-android) or
[simple-build-tool](http://groups.google.com/group/simple-build-tool).

There is also an IRC channel at `#sbt-android` on
[Freenode](http://www.freenode.net).

# Installation

The basic requirements are :

  * [SBT](http://www.scala-sbt.org/) (at least version 0.12.0)

    - On OSX, install it with [Homebrew](http://mxcl.github.io/homebrew/) :
      `brew install sbt`

    - Follow the [installation
      instructions](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)
      for other systems

  * The [Android SDK](http://developer.android.com/sdk/index.html)

    - On OSX, install it with [Homebrew](http://mxcl.github.io/homebrew/) :
      `brew install android-sdk`

    - On some Linux distributions, you can find it in third-party repositories
      (in Archlinux, for instance, there is an [AUR
      package](https://aur.archlinux.org/packages/android-sdk/))

    - Alternatively, you can download the archive, unzip it and run the SDK
      installer directly
