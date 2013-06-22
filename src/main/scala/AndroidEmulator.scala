package sbtandroid

import sbt._
import Keys._

import AndroidPlugin._
import AndroidHelpers.isWindows

import complete.DefaultParsers._

object AndroidEmulator {

  private val listDevicesTask = Def.task { dbPath.value + " devices" !; () }

  private val killAdbTask = Def.task { dbPath.value +" kill-server" !; () }

  private val emulatorStopTask = Def.task {
    streams.value.log.info("Stopping emulators")
    val serial = "%s -e get-serialno".format(dbPath.value).!!
    "%s -s %s emu kill".format(dbPath.value, serial).!
    ()
  }

  private val emulatorStartTask = Def.inputTask {
    "%s/emulator -avd %s".format(toolsPath.value,
                                 installedAvds.parsed).run; ()
  }

  val installedAvds = Def.setting {
    (s: State) => {
      // List available AVDs
      val avds = ((Path.userHome / ".android" / "avd" * "*.ini") +++
        (if (isWindows) (sdkPath.value / ".android" / "avd" * "*.ini")
         else PathFinder.empty)).get

      // Parse the AVD name
      Space ~> avds.map(f => token(f.base))
                   .reduceLeftOption(_ | _).getOrElse(token("none"))
     }
  }

  lazy val baseSettings: Seq[Setting[_]] = (Seq(
    listDevices <<= listDevicesTask,
    killAdb <<= killAdbTask,
    emulatorStart <<= emulatorStartTask,
    emulatorStop <<= emulatorStopTask
  ))

  lazy val aggregateSettings: Seq[Setting[_]] = Seq(
    listDevices,
    emulatorStart,
    emulatorStop
  ) map { aggregate in _ := false }

  lazy val settings: Seq[Setting[_]] = baseSettings ++ aggregateSettings
}
