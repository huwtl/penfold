organization := "com.qmetric"

name := "penfold"

version := "0.1.1"

scalaVersion := "2.10.3"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature" )

seq(webSettings :_*)

conflictWarning in ThisBuild := ConflictWarning.disable

parallelExecution in Global := false

publishMavenStyle := true

publishArtifact in Test := false

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra_2.10" % "2.2.2",
  "org.scalatra" % "scalatra-scalate_2.10" % "2.2.2",
  "ch.qos.logback" % "logback-classic" % "1.0.7" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar"),
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "com.theoryinpractise" % "halbuilder-core" % "2.0.2",
  "org.json4s" %% "json4s-jackson" % "3.2.4",
  "com.typesafe" % "scalalogging-slf4j_2.10" % "1.1.0",
  "net.ceedubs" % "ficus_2.10" % "1.0.0",
  "org.scalatra" %% "scalatra-auth" % "2.2.2",
  "com.typesafe.slick" % "slick_2.10" % "2.0.1",
  "c3p0" % "c3p0" % "0.9.1.2",
  "mysql" % "mysql-connector-java" % "5.1.30",
  "org.mongodb" %% "casbah" % "2.7.0",
  "com.googlecode.flyway" % "flyway-core" % "2.3.1",
  "org.hsqldb" % "hsqldb" % "2.3.2",
  "com.codahale.metrics" % "metrics-healthchecks" % "3.0.2",
  "org.mockito" % "mockito-all" % "1.9.0" % "test",
  "org.specs2" % "specs2_2.10" % "2.1.1" % "test",
  "org.scalatra" %% "scalatra-specs2" % "2.2.2" % "test",
  "com.github.athieriot" %% "specs2-embedmongo" % "0.6.0" % "test"
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "clojars.org" at "http://clojars.org/repo/"

resolvers += Classpaths.sbtPluginReleases

ScoverageSbtPlugin.instrumentSettings

CoverallsPlugin.singleProject

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/qmetric/penfold</url>
  <licenses>
    <license>
      <name>Apache 2.0 License</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:qmetric/penfold.git</url>
    <connection>scm:git:git@github.com:qmetric/penfold.git</connection>
  </scm>
  <developers>
    <developer>
      <id>huwtl</id>
      <name>Huw Lewis</name>
      <url>https://github.com/huwtl</url>
    </developer>
  </developers>
)
