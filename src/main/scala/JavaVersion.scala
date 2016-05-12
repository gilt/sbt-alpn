package com.gilt.sbt.alpn

import java.util.regex.Pattern

case class JavaVersion(major: Int, minor: Int, micro: Int, patch: Int)

object JavaVersion {
  val VersionPattern = Pattern.compile("^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:_([0-9]+))?(?:-.+)?$")

  def apply(): JavaVersion = {
    val versionStr = System.getProperty("java.version", "")
    val m = VersionPattern.matcher(versionStr)
    if (!m.matches()) {
      JavaVersion(0,0,0,0)
    } else {
      JavaVersion(
        m.group(1).toInt,
        m.group(2).toInt,
        m.group(3).toInt,
        Option(m.group(4)).map(_.toInt).getOrElse(0)
      )
    }
  }

}
