organization := "com.qmetric"

name := "penfold"

version := "1.0.0"

scalaVersion := "2.11.6"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature" )

seq(webSettings :_*)

conflictWarning in ThisBuild := ConflictWarning.disable

parallelExecution in Global := false

publishMavenStyle := true

publishArtifact in Test := false

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra_2.11" % "2.3.1",
  "org.scalatra" % "scalatra-scalate_2.11" % "2.3.1",
  "org.scalatra" % "scalatra-jetty_2.11" % "2.3.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.3.0.M2" % "container,compile",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.theoryinpractise" % "halbuilder-core" % "4.0.3",
  "com.theoryinpractise" % "halbuilder-json" % "4.0.2",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0",
  "net.ceedubs" % "ficus_2.11" % "1.1.2",
  "org.scalatra" %% "scalatra-auth" % "2.3.1",
  "com.typesafe.slick" % "slick_2.11" % "2.1.0",
  "com.github.tminglei" % "slick-pg_2.11" % "0.8.5",
  "com.vividsolutions" % "jts" % "1.13",
  "c3p0" % "c3p0" % "0.9.1.2",
  "com.googlecode.flyway" % "flyway-core" % "2.3.1",
  "com.codahale.metrics" % "metrics-healthchecks" % "3.0.2",
  "me.moocar" % "logback-gelf" % "0.12",
  "org.hsqldb" % "hsqldb" % "2.3.2" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.specs2" % "specs2_2.11" % "2.4.15" % "test",
  "org.scalatra" %% "scalatra-specs2" % "2.3.1" % "test",
  "org.hamcrest" % "hamcrest-core" % "1.3" % "test",
  "com.opentable.components" % "otj-pg-embedded" % "0.3.0" % "test"
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers += "clojars.org" at "http://clojars.org/repo/"

resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

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
