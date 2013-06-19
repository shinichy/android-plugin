package sbtandroid

import java.util.Properties
import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser, ConfigurationWriter}

import sbt._
import Keys._
import AndroidPlugin._
import AndroidHelpers._

import java.io.{File => JFile}

object AndroidInstall {

  /**
   * Task that installs a package on the target
   */
  private val installTask =
    (adbTarget, dbPath, packageApkPath, streams) map { (t, dp, p, s) =>
    s.log.info("Installing %s".format(p.name))
    t.installPackage(dp, s, p)
    ()
  }

  /**
   * Task that uninstalls a package from the target
   */
  private val uninstallTask =
    (adbTarget, dbPath, packageApkPath, streams) map { (t, dp, p, s) =>
    s.log.info("Uninstalling %s".format(p.name))
    t.uninstallPackage(dp, s, p)
    ()
  }

  private def aaptPackageTask = Def.task {

    // Make assets directory
    mainAssetsPath.value.mkdirs

    // Resource arguments
    val libraryResPathArgs = resPath.value.flatMap(p => Seq("-S", p.absolutePath))

    // AAPT command line
    val aapt = Seq(aaptPath.value.absolutePath,
        "package", "--auto-add-overlay", "-f",
        "-M", manifestPath.value.head.absolutePath,
        "-A", mainAssetsPath.value.absolutePath,
        "-I", libraryJarPath.value.absolutePath,
        "-F", resourcesApkPath.value.absolutePath) ++
        libraryResPathArgs

    // Package resources
    streams.value.log.info("Packaging resources in " + resourcesApkPath.value.absolutePath)
    streams.value.log.debug("Running: " + aapt.mkString(" "))
    if (aapt.run(false).exitValue != 0) sys.error("Error packaging resources")

    // Return the path to the resources APK
    resourcesApkPath.value
  }

  private def dxTask = Def.task {

      // Main dex command
      def dexing(inputs: Seq[JFile], output: JFile) {
        val uptodate = output.exists && inputs.forall(input =>
          input.isDirectory match {
            case true =>
              (input ** "*").get.forall(_.lastModified <= output.lastModified)
            case false =>
              input.lastModified <= output.lastModified
          }
        )

        if (!uptodate) {
          val noLocals = if (proguardOptimizations.value.isEmpty) "" else "--no-locals"
          val dxCmd = (Seq(dxPath.value.absolutePath,
                          dxMemoryParameter(dxMemory.value),
                          "--dex", noLocals,
                          "--num-threads=" + java.lang.Runtime.getRuntime.availableProcessors,
                          "--output=" + output.getAbsolutePath) ++
                          inputs.map(_.absolutePath)).filter(_.length > 0)
          streams.value.log.debug(dxCmd.mkString(" "))
          streams.value.log.info("Dexing "+output.getAbsolutePath)
          streams.value.log.debug(dxCmd !!)
        } else streams.value.log.debug("Dex file " + output.getAbsolutePath + "is up to date, skipping")
      }

      // First, predex the inputs in dxPredex
      val dxPredexInputs = dxInputs.value filter (dxPredex.value contains _) map { jarPath =>

        // Generate the output path
        val outputPath = target.value / (jarPath.getName + ".apk")

        // Predex the library
        dexing(Seq(jarPath), outputPath)

        // Return the output path
        outputPath
      }

      // Non-predexed inputs
      val dxClassInputs = dxInputs.value filterNot (dxPredex.value contains _)

      // Generate the final DEX
      dexing(dxClassInputs +++ dxPredexInputs get, dxOutputPath.value)

      // Return the path to the generated final DEX file
      dxOutputPath.value
    }

  private def proguardTask = Def.task {

      proguardConfiguration.value map { configFile =>
        // Execute Proguard
        streams.value.log.info(
          "Executing Proguard with configuration file " + configFile.getAbsolutePath)

        // Parse the configuration
        val config = new ProGuardConfiguration
        val parser = new ConfigurationParser(configFile, new Properties)
        parser.parse(config)

        // Execute ProGuard
        val proguard = new ProGuard(config)
        proguard.execute

        // Return the proguard-ed output JAR
        proguardOutputPath.value
      }
  }

  private def proguardConfigurationTask = Def.task {

      if (useProguard.value) {

          val generatedOptions =
            if(generatedProguardConfigPath.value.exists)
              scala.io.Source.fromFile(generatedProguardConfigPath.value).getLines.filterNot(x => x.isEmpty || x.head == '#').toSeq
            else Seq()

          val optimizationOptions = if (proguardOptimizations.value.isEmpty) Seq("-dontoptimize")
                                    else proguardOptimizations.value

          val manifestr = List("!META-INF/MANIFEST.MF", "R.class", "R$*.class",
                               "TR.class", "TR$.class", "library.properties")
          val sep = JFile.pathSeparator

          // Input class files
          val inClass = "\"" + classDirectory.value.absolutePath + "\""

          // Input library JARs to be included in the APK
          val inJars = includedClasspath.value
                       .map("\"" +_ + "\"" + manifestr.mkString("(", ",!**/", ")"))
                       .mkString(sep)

          // Input library JARs to be provided at runtime
          val inLibrary = providedClasspath.value
                          .map("\"" + _.absolutePath + "\"")
                          .mkString(sep)

          // Output JAR
          val outJar = "\""+proguardOutputPath.value.absolutePath+"\""

          // Proguard arguments
          val args = (
                 "-injars" :: inClass ::
                 "-injars" :: inJars ::
                 "-outjars" :: outJar ::
                 "-libraryjars" :: inLibrary ::
                 Nil) ++
                 generatedOptions ++
                 optimizationOptions ++ (
                 "-dontwarn" :: "-dontobfuscate" ::
                 "-dontnote scala.Enumeration" ::
                 "-dontnote org.xml.sax.EntityResolver" ::
                 "-keep public class * extends android.app.backup.BackupAgent" ::
                 "-keep public class * extends android.appwidget.AppWidgetProvider" ::
                 "-keep class scala.collection.SeqLike { public java.lang.String toString(); }" ::
                 "-keep class scala.reflect.ScalaSignature" ::
                 "-keep public class " + manifestPackage.value + ".** { public protected *; }" ::
                 "-keep public class * implements junit.framework.Test { public void test*(); }" ::
                 """
                  -keepclassmembers class * implements java.io.Serializable {
                    private static final java.io.ObjectStreamField[] serialPersistentFields;
                    private void writeObject(java.io.ObjectOutputStream);
                    private void readObject(java.io.ObjectInputStream);
                    java.lang.Object writeReplace();
                    java.lang.Object readResolve();
                   }
                   """ :: Nil) ++ proguardOptions.value

          // Instantiate the Proguard configuration
          val config = new ProGuardConfiguration
          new ConfigurationParser(args.toArray[String], new Properties).parse(config)

          // Write that to a file
          val configFile = sourceManaged.value / "proguard.txt"
          val writer = new ConfigurationWriter(configFile)
          writer.write(config)
          writer.close

          // Return the configuration file
          Some(configFile)

        } else None
    }

  private val apkTask =
    (useDebug, packageConfig, streams) map { (debug, c, s) =>
      val builder = new ApkBuilder(c, debug)
      builder.build.fold(sys.error(_), s.log.info(_))
      s.log.debug(builder.outputStream.toString)
      c.packageApkPath
    }

  lazy val settings: Seq[Setting[_]] = Seq(

    // Resource package generation
    aaptPackage <<= aaptPackageTask,

    // Dexing (DX)
    dx <<= dxTask,

    // Clean generated APK
    cleanApk <<= (packageApkPath) map (IO.delete(_)),

    // Proguard
    proguard <<= proguardTask,
    proguard <<= proguard dependsOn (compile),

    // Proguard configuration
    proguardConfiguration <<= proguardConfigurationTask,

    // Final APK generation
    packageConfig <<=
      (toolsPath, packageApkPath, resourcesApkPath, dxOutputPath,
       nativeDirectories, dxInputs, resourceDirectory) map
      (ApkConfig(_, _, _, _, _, _, _)),

    apk <<= apkTask dependsOn (cleanApk, aaptPackage, dx, copyNativeLibraries),

    // Package installation
    install <<= installTask dependsOn apk,

    // Package uninstallation
    uninstall <<= uninstallTask
  )
}
