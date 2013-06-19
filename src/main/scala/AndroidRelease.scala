package sbtandroid

import sbt._

import Keys._
import AndroidPlugin._

object AndroidRelease {

  def zipAlignTask = Def.task {
      val zipAlign = Seq(
          zipAlignPath.value.absolutePath,
          "-v", "4",
          packageApkPath.value.absolutePath,
          packageAlignedPath.value.absolutePath)

      streams.value.log.debug("Aligning " + zipAlign.mkString(" "))
      streams.value.log.debug(zipAlign !!)
      streams.value.log.info("Aligned " + packageAlignedPath.value)
      packageAlignedPath.value
    }

   def signReleaseTask = Def.task {
      val jarsigner = Seq(
        "jarsigner",
        "-verbose",
        "-keystore", keystorePath.value.absolutePath,
        "-storepass", PasswordManager.get(
          "keystore", keyalias.value, cachePasswords.value).getOrElse(
            sys.error("could not get password")),
        packageApkPath.value.absolutePath,
        keyalias.value)

      streams.value.log.debug("Signing " + jarsigner.mkString(" "))
      val out = new StringBuffer
      val exit = jarsigner.run(new ProcessIO(input => (),
                            output => out.append(IO.readStream(output)),
                            error  => out.append(IO.readStream(error)),
                            inheritedInput => false)
                        ).exitValue()
      if (exit != 0) sys.error("Error signing: "+out)
      streams.value.log.debug(out.toString)
      streams.value.log.info("Signed " + packageApkPath.value)
      packageApkPath.value
    }

  private def releaseTask = (packageAlignedPath, streams) map { (path, s) =>
    s.log.success("Ready for publication: \n" + path)
    path
  }

  lazy val settings: Seq[Setting[_]] = (Seq(
    // Configuring Settings
    keystorePath := Path.userHome / ".keystore",
    zipAlignPath <<= (toolsPath) { _ / "zipalign" },
    packageAlignedName <<= (artifact, versionName) map ((a,v) =>
                                                String.format("%s-signed-%s.apk", a.name, v)),
    packageAlignedPath <<= (target, packageAlignedName) map ( _ / _ ),

    // Configuring Tasks
    cleanAligned <<= (packageAlignedPath) map (IO.delete(_)),

    release <<= releaseTask,
    release <<= release dependsOn zipAlign,

    zipAlign <<= zipAlignTask,
    zipAlign <<= zipAlign dependsOn (signRelease, cleanAligned),

    signRelease <<= signReleaseTask,
    signRelease <<= signRelease dependsOn apk
  ))
}
