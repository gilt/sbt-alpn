sbt-alpn
=================

Adds the ALPN Agent JAR to builds using sbt-native-packager.

Prerequisites
-------------
The plugin assumes that sbt-native-packager has been included in your SBT build configuration.

Installation for sbt-native-packager 1.0.x (and Play 2.4.x)
------------

Add the following to your `project/plugins.sbt` file:

```scala
addSbtPlugin("com.gilt.sbt" % "sbt-alpn" % "0.0.1")
```

To use the ALPN agent settings in your project, add the `JettyAlpn` auto-plugin to your project.

```scala
enablePlugins(JettyAlpn)
```

Configuration
-------------

To use a specific ALPN agent version, add the following to your `build.sbt` file:

```scala
alpnAgentVersion := "2.0.2"
```

To include the ALPN api, on the classpath add the following to your `build.sbt` file:

```scala
alpnApiIncluded := true
alpnApiVersion := "1.1.2.v20150522"
```
