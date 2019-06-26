import Dependencies._

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val javaConcurrentProgramming = (project in file("javaconcurrentprogramming"))
lazy val zio = project
  .settings(
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.scalaz" %% "scalaz-zio" % "1.0-RC4"
    ))

//lazy val root = (project in file("."))
//  .settings(
//    name := "Concurrent Programming",
//    libraryDependencies ++= Seq(
//      scalaTest % Test,
//      "org.scalaz" %% "scalaz-zio" % "1.0-RC4"
//    ))

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
