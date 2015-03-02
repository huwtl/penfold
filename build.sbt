organization := "org.huwtl"

name := "penfold"

version := "0.1.8-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature" )

seq(webSettings :_*)

conflictWarning in ThisBuild := ConflictWarning.disable

parallelExecution in Global := false

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra_2.10" % "2.2.2",
  "org.scalatra" % "scalatra-scalate_2.10" % "2.2.2",
  "ch.qos.logback" % "logback-classic" % "1.0.7" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar"),
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "com.theoryinpractise" % "halbuilder-core" % "4.0.3",
  "com.theoryinpractise" % "halbuilder-json" % "4.0.2",
  "org.json4s" %% "json4s-jackson" % "3.2.4",
  "com.typesafe" % "scalalogging-slf4j_2.10" % "1.1.0",
  "net.ceedubs" % "ficus_2.10" % "1.0.0",
  "org.scalatra" %% "scalatra-auth" % "2.2.2",
  "com.typesafe.slick" % "slick_2.10" % "2.0.1",
  "com.github.tminglei" % "slick-pg_2.10" % "0.8.1",
  "com.vividsolutions" % "jts" % "1.13",
  "c3p0" % "c3p0" % "0.9.1.2",
  "mysql" % "mysql-connector-java" % "5.1.30",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "com.googlecode.flyway" % "flyway-core" % "2.3.1",
  "com.typesafe.akka" %% "akka-actor" % "2.2.4",
  "com.codahale.metrics" % "metrics-healthchecks" % "3.0.2",
  "me.moocar" % "logback-gelf" % "0.10p1",
  "org.hsqldb" % "hsqldb" % "2.3.2" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test",
  "org.specs2" % "specs2_2.10" % "2.1.1" % "test",
  "org.scalatra" %% "scalatra-specs2" % "2.2.2" % "test",
  "com.opentable.components" % "otj-pg-embedded" % "0.3.0" % "test"
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "clojars.org" at "http://clojars.org/repo/"

resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

ScoverageSbtPlugin.instrumentSettings

CoverallsPlugin.singleProject
