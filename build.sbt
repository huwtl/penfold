organization := "com.hlewis"

name := "event-fire"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.1"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature" )

seq(webSettings :_*)

conflictWarning in ThisBuild := ConflictWarning.disable

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra_2.10" % "2.2.0",
  "org.scalatra" % "scalatra-scalate_2.10" % "2.2.0",
  "org.scalatra" % "scalatra-scalatest_2.10" % "2.2.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.7" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar"),
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "com.github.philcali" % "cronish_2.10" % "0.1.3",
  "org.scalaj" % "scalaj-time_2.10.0-M7" % "0.6",
  "com.theoryinpractise" % "halbuilder-core" % "2.0.2",
  "org.json4s" %% "json4s-jackson" % "3.2.4"
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"