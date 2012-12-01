organization := "com.hlewis"

name := "rabbit-scheduler"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

classpathTypes ~= (_ + "orbit")

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra" % "2.1.1",
  "org.scalatra" % "scalatra-scalate" % "2.1.1",
  "org.scalatra" % "scalatra-scalatest" % "2.1.1" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.7" % "runtime",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container" artifacts (Artifact("javax.servlet", "jar", "jar")),
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.3.v20120416" % "container" artifacts (Artifact("jetty-webapp", "jar", "jar")),
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "net.liftweb" %% "lift-json" % "2.5-M1",
  "net.debasishg" %% "redisclient" % "2.7",
  "com.google.inject" % "guice" % "3.0",
  "com.google.inject.extensions" % "guice-servlet" % "3.0",
  "net.liftweb" % "lift-amqp_2.9.1" % "2.4",
  "com.github.philcali" %% "cronish" % "0.1.1",
  "org.scalaj" %% "scalaj-time" % "0.6"
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"