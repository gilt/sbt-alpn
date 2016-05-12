package com.gilt.sbt.alpn

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport.bashScriptExtraDefines

object JettyAlpn extends AutoPlugin {
  object autoImport {
    val alpnApiIncluded = settingKey[Boolean]("Include ALPN api as a dependency")
    val alpnApiVersion = settingKey[String]("ALPN api version")
    val alpnAgentVersion = settingKey[String]("ALPN boot version")
    val alpnAgent = taskKey[File]("ALPN agent jar location")
  }

  import autoImport._

  override def requires = JavaAppPackaging

  val alpnConfig = config("alpn-agent").hide

  override lazy val projectSettings = Seq(
    ivyConfigurations += alpnConfig,
    alpnApiIncluded := false,
    alpnApiVersion := "1.1.2.v20150522",
    alpnAgentVersion := "2.0.2",
    alpnAgent := findAlpnAgent(update.value),
    libraryDependencies += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % alpnAgentVersion.value % alpnConfig,
    mappings in Universal += alpnAgent.value -> "alpn/jetty-alpn-agent.jar",
    bashScriptExtraDefines += """addJava "-javaagent:${app_home}/../alpn/jetty-alpn-agent.jar""""
  ) ++ {
    if(alpnApiIncluded.value) Seq(libraryDependencies += "org.eclipse.jetty.alpn" % "alpn-api" % alpnApiVersion.value)
    else Seq.empty
  }

  private[this] val alpnAgentFilter: DependencyFilter =
    configurationFilter("alpn-agent") && artifactFilter(`type` = "jar")

  def findAlpnAgent(report: UpdateReport) = report.matching(alpnAgentFilter).head
}

