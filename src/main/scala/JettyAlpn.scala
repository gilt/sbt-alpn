package com.gilt.sbt.alpn

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport.bashScriptExtraDefines

object JettyAlpn extends AutoPlugin {
  object autoImport {
    val alpnDownloadBootVersion = settingKey[Option[String]]("ALPN api version")
    val alpnApiIncluded = settingKey[Boolean]("Include ALPN api as a dependency")
    val alpnApiVersion = settingKey[String]("ALPN api version")
    val alpnAgentVersion = settingKey[String]("ALPN boot version")
    val alpnAgent = taskKey[File]("ALPN agent jar location")
    val alpnDownloadBoot = taskKey[Option[File]]("ALPN agent jar location")
  }

  import autoImport._

  override def requires = JavaAppPackaging

  val alpnAgentConfig = config("alpn-agent").hide
  val alpnBootConfig = config("alpn-boot").hide

  override lazy val projectSettings = Seq(
    ivyConfigurations ++= Seq(alpnAgentConfig, alpnBootConfig),
    alpnApiIncluded := false,
    alpnDownloadBootVersion := Some("8.1.8.v20160420"),
    alpnApiVersion := "1.1.2.v20150522",
    alpnAgentVersion := "2.0.2",
    alpnAgent := findAlpnAgent(update.value),
    alpnDownloadBoot := findAlpnBoot(update.value),
    libraryDependencies ++= {
      val agentAndApi = if(alpnApiIncluded.value) {
        Seq(
          "org.eclipse.jetty.alpn" % "alpn-api" % alpnApiVersion.value,
          "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnAgentVersion.value % alpnAgentConfig
        )
      } else {
        Seq("org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnAgentVersion.value % alpnAgentConfig)
      }

      val boot = if(alpnDownloadBootVersion.value.isDefined) {
        Seq("org.mortbay.jetty.alpn" % "alpn-boot" % alpnDownloadBootVersion.value.get % alpnBootConfig)
      } else { Seq.empty }

      agentAndApi ++ boot
    },
    mappings in Universal ++= {
      alpnDownloadBoot.value.map { alpnDownloadBootVal =>
        Seq(
          alpnAgent.value -> "alpn/jetty-alpn-agent.jar",
          alpnDownloadBootVal -> s"alpn/alpn-boot-${alpnDownloadBootVersion.value.get}.jar"
        )
      }.getOrElse(Seq(alpnAgent.value -> "alpn/jetty-alpn-agent.jar"))
    },
    bashScriptExtraDefines += """addJava "-javaagent:${app_home}/../alpn/jetty-alpn-agent.jar""""
  )

  private[this] val alpnAgentFilter: DependencyFilter =
    configurationFilter("alpn-agent") && artifactFilter(`type` = "jar")

  private[this] val alpnBootFilter: DependencyFilter =
    configurationFilter("alpn-boot") && artifactFilter(`type` = "jar")

  def findAlpnAgent(report: UpdateReport): File = report.matching(alpnAgentFilter).head

  def findAlpnBoot(report: UpdateReport): Option[File] = report.matching(alpnBootFilter).headOption
}

