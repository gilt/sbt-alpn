package com.gilt.sbt.alpn

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport.bashScriptExtraDefines

object JettyAlpn extends AutoPlugin {
  object autoImport {
    val alpnDownloadBootVersion = settingKey[Option[String]]("ALPN boot version")
    val alpnDownloadBootAutoVersion = settingKey[String]("ALPN auto generated boot version")
    val alpnApiIncluded = settingKey[Boolean]("Include ALPN api as a dependency")
    val alpnApiVersion = settingKey[String]("ALPN api version")
    val alpnBoot = taskKey[File]("ALPN boot jar location")
  }

  import autoImport._

  override def requires = JavaAppPackaging

  val alpnBootConfig = config("alpn-boot").hide

  override lazy val projectSettings = Seq(
    ivyConfigurations += alpnBootConfig,
    alpnApiIncluded := false,
    alpnDownloadBootVersion := None,
    alpnDownloadBootAutoVersion := alpnDownloadBootVersion.value.getOrElse(findArtifactVersion(AlpnMappings)),
    alpnApiVersion := "1.1.2.v20150522",
    alpnBoot := findAlpnBoot(update.value),
    libraryDependencies ++= {
      if(alpnApiIncluded.value) {
        Seq(
          "org.eclipse.jetty.alpn" % "alpn-api" % alpnApiVersion.value,
          "org.mortbay.jetty.alpn" % "alpn-boot" % alpnDownloadBootAutoVersion.value % alpnBootConfig
        )
      } else {
        Seq("org.mortbay.jetty.alpn" % "alpn-boot" % alpnDownloadBootAutoVersion.value % alpnBootConfig)
      }
    },
    mappings in Universal += alpnBoot.value -> s"alpn/alpn-boot-${alpnDownloadBootAutoVersion.value}.jar",
    bashScriptExtraDefines += s"""addJava "-Xbootclasspath/p:$${app_home}/../alpn/alpn-boot-${alpnDownloadBootAutoVersion.value}.jar""""
  )

  private val AlpnMappings = Seq(
    new VersionMapping("8.1.9.v20160720", 1, 8, 0, 102),
    new VersionMapping("8.1.9.v20160720", 1, 8, 0, 101),
    new VersionMapping("8.1.8.v20160420", 1, 8, 0, 92),
    new VersionMapping("8.1.7.v20160121", 1, 8, 0, 91),
    new VersionMapping("8.1.7.v20160121", 1, 8, 0, 77),
    new VersionMapping("8.1.7.v20160121", 1, 8, 0, 74),
    new VersionMapping("8.1.7.v20160121", 1, 8, 0, 73),
    new VersionMapping("8.1.7.v20160121", 1, 8, 0, 72),
    new VersionMapping("8.1.7.v20160121", 1, 8, 0, 71),
    new VersionMapping("8.1.6.v20151105", 1, 8, 0, 66),
    new VersionMapping("8.1.6.v20151105", 1, 8, 0, 65),
    new VersionMapping("8.1.5.v20150921", 1, 8, 0, 60),
    new VersionMapping("8.1.4.v20150727", 1, 8, 0, 51),
    new VersionMapping("8.1.3.v20150130", 1, 8, 0, 45),
    new VersionMapping("8.1.3.v20150130", 1, 8, 0, 40),
    new VersionMapping("8.1.3.v20150130", 1, 8, 0, 31),
    new VersionMapping("8.1.2.v20141202", 1, 8, 0, 25),
    new VersionMapping("8.1.0.v20141016", 1, 8, 0, 20),
    new VersionMapping("8.1.0.v20141016", 1, 8, 0, 11),
    new VersionMapping("8.1.0.v20141016", 1, 8, 0, 05),
    new VersionMapping("8.1.0.v20141016", 1, 8, 0, 0),
    new VersionMapping("7.1.3.v20150130", 1, 7, 0, 80),
    new VersionMapping("7.1.3.v20150130", 1, 7, 0, 79),
    new VersionMapping("7.1.3.v20150130", 1, 7, 0, 76),
    new VersionMapping("7.1.3.v20150130", 1, 7, 0, 75),
    new VersionMapping("7.1.2.v20141202", 1, 7, 0, 72),
    new VersionMapping("7.1.2.v20141202", 1, 7, 0, 71),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 67),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 65),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 60),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 55),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 51),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 45),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 40),
    new VersionMapping("7.1.0.v20141016", 1, 7, 0, 0)
  )

  private def findArtifactVersion(maps: Seq[VersionMapping]): String = maps.find(_.matches).get.artifactVersion

  private[this] val alpnBootFilter: DependencyFilter =
    configurationFilter("alpn-boot") && artifactFilter(`type` = "jar")

  def findAlpnBoot(report: UpdateReport): File = report.matching(alpnBootFilter).head

}

case class VersionMapping(artifactVersion: String, major: Int, minor: Int, micro: Int, startPatch: Int) {
  def matches = {
    val javaVersion = JavaVersion()
    (javaVersion.major == major) &&
      (javaVersion.minor == minor) &&
      (javaVersion.micro == micro) &&
      (javaVersion.patch >= startPatch)
  }
}
