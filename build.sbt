import Dependencies._

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  libraryDependencies += scalaTest % Test
)

lazy val javaConcurrentProgramming = (project in file("javaconcurrentprogramming"))
lazy val zio = project
  .settings(
    commonSettings,
    libraryDependencies += "org.scalaz" %% "scalaz-zio" % "1.0-RC4"
    )
val catsEffect = (project in file("catseffect"))
  .settings(
    commonSettings,
    libraryDependencies += "org.typelevel" %% "cats-effect" % "1.3.1",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-language:postfixOps",
      "-language:higherKinds",
      "-Ypartial-unification")
  )

//lazy val root = (project in file("."))
//  .settings(
//    name := "Concurrent Programming",
//    libraryDependencies ++= Seq(
//      scalaTest % Test,
//      "org.scalaz" %% "scalaz-zio" % "1.0-RC4"
//    ))

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
