import AssemblyKeys._

organization := "com.github.brisk"

name := "brisk"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "scala-tools" at "http://scala-tools.org/repo-releases",
  "sonatype-snapshots" at "https://oss.sonatype.org/content/groups/public",
  "maven" at "http://repo1.maven.org/maven2"
)

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "2.9.1",
  //"com.codahale" %% "jerkson" % "0.5.0",
  "ch.qos.logback" % "logback-classic" % "0.9.30" % "compile",
  "io.netty" % "netty" % "3.5.11.Final",
  "org.jgroups" % "jgroups" % "3.2.4.Final",
  "org.xerial.snappy" % "snappy-java" % "1.0.5-M3",
  "org.scalatest" % "scalatest_2.10.0"  % "1.8"  % "test"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  //"-Xmigration",
  "-Xexperimental",
  "-Xcheckinit",
  "-optimise",
  "-encoding", "utf8"
)

javacOptions ++= Seq("-source", "1.7")

publishTo <<= (version) { version: String =>
  val nexus = "https://oss.sonatype.org/content/repositories/"
  if (version.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "snapshots/")
  else
    Some("releases" at nexus + "releases/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

seq(assemblySettings: _*)

test in assembly := {}

