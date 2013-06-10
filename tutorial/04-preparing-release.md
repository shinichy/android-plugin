---
layout: post
title: 4. Preparing a release
subtitle: Setting your app live!
---

# Generating a key

First of all, you need to sign your app. For this, you will need a private key.
You can generate one using the `keytool` program which is supplied with the
JDK, as follows:

```
keytool -genkey -v -keystore ~/.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
```

This will save a `.keystore` file in your home directory, which contains all
kinds of super-secret private key stuff. Make a secure copy of this somewhere,
because you'll need it throughout your Android career!

Note that SBT will assume that the file is called `~/.keystore`. If you decide
to name it differently, SBT will not be able to sign your app!

Also, remember the alias that you choose on the command-line. You'll need it in
the next section.

For more information about keys and signing, see the [Android Developers
site](http://developer.android.com/guide/publishing/app-signing.html).

# Updating your configuration

Next, you have to update your build script, which is located in `build.sbt` or
`project/build.scala`. Add this line to your settings :

```scala
keyalias := "alias_name",
```

If you used a different `alias_name` when generating the keystore, modify this
line accordingly. If you still have an open SBT session at this point: exit and
restart, because it doesn't refresh your new build settings automatically.

# Preparing your release

In SBT's interactive prompt, just run the `release` command.

_**Note:** Your password will be displayed in plain text on your screen, so don't do
this in a crowded room or anything. (If `sign` fails with
"sign-release: Nonzero exit value", try an alias without a '-')_

In the text that `release` outputs to the console, you will see the path to the
APK file that you can upload to the Play Store. It's in the `target/` folder
and it looks like `name-signed-0.1.apk`.

# Releasing your release

Now that you have a properly signed APK file, you can release your app. Go to
[Android Developer Console](https://play.google.com/apps/publish/) and follow the
instructions from there.

You will be asked to upload your APK file, and also to write a description,
upload some screenshots (at least 2), and upload some graphics to display in
the Play Store, so make sure you have these handy!

Once the app is uploaded, it's available (almost) immediately, although it
might take a while until it finally shows up in the search. If you're
impatient, you can access it directly using the following url:
`https://play.google.com/store/apps/details?id=<package_name>` where `<package_name>`
is the Java package name for your app.

And that's it! You are now able to publish your app in the Android Play Store!

# Updating your app

So, your app has been on the Play Store for some time, and you're ready to
release version 2. Awesome! Here's how you do it.

  * Write version 2, obviously.

  * Bump the `version` and `versionCode` settings. Remember that `versionCode`
    is for internal use only and must increase by at least one, each time you
    release. `version` is the number that users may see, and could even remain the
    same (although I don't recommend that).

  * Then, re-build and sign your app as described above, and upload it!


&nbsp;

&nbsp;

Disclaimer: text copied with permission from [here](http://blog.jqno.nl/howto-release-your-scala-android-app-to-the-m)
