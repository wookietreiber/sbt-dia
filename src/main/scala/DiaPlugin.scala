package sbtx.doc.dia

import sbt._
import Keys._
import sys.process._

object DiaPlugin extends Plugin {
  import DiaKeys._

  object DiaKeys {
    val dia = TaskKey[Unit]("dia",
      "Exports '*.dia' files.")

    val diaSource = SettingKey[File]("dia-source",
      "Source directory where '*.dia' files are located.")

    val diaOutput = SettingKey[(String,Option[String])]("dia-output",
      "Which output format (and optionally which filter) to use for the export.")
  }

  lazy val diaSettings = Seq (
    sourceDirectory             in dia <<=  sourceDirectory in Compile,
    diaSource                   in dia <<=  (sourceDirectory in dia) / "dia",
//    unmanagedSourceDirectories  in dia <<=  Seq(diaSource).join,
//    sourceDirectories           in dia <<=  unmanagedSourceDirectories in dia,
    sources                     in dia <<=  (diaSource in dia) map { _ ** "*.dia" get },
    watchSources                       <++= sources in dia,
    target                      in dia <<=  target ( _ / "dia" ),

    diaOutput := ("png", None),

    dia <<= (sources in dia, target in dia, diaOutput) map { (sources, target, ff) =>
      val (format, filter) = ff

      target.mkdirs()

      sources foreach { source =>
        val output = target / ((source.getName dropRight 3) + format)

        var diaCmd = "dia %s --export=%s" format (source, output)
        filter foreach { f =>
          diaCmd += (" --filter=%s" format f)
        }

        diaCmd !
      }
    }
  )

}
