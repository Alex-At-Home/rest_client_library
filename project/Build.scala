import sbt._
import Keys._
import sbtassembly.AssemblyKeys._
import sbtassembly.{MergeStrategy, PathList}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object BuildSettings {

  val scalaBuildVersion = "2.11.8"

  val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    organization := "org.elastic",
    scalacOptions ++= Seq(),
    scalaVersion := scalaBuildVersion,
    crossScalaVersions := Seq(),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
}

object MyBuild extends Build {
  import BuildSettings._

  // Dependencies:

  val circeVersion = "0.4.1"
  lazy val circeDeps = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

  val utestJvmVersion = "0.4.3"
  lazy val utestJvmDeps = "com.lihaoyi" %% "utest" % utestJvmVersion % "test"

  // Project definitions

  val restScalaDriverVersion = "0.1-SNAPSHOT"

  lazy val root = Project(
    "root",
    file("."),
    settings = buildSettings
  )
  .enablePlugins(ScalaJSPlugin)
  .aggregate(
    rest_scala_core_JVM,
    rest_json_circe_module
  )

  val githubName = "rest_client_library"
  val apiRoot = "https://alex-at-home.github.io"
  val docVersion = "current"

  lazy val rest_scala_core = crossProject
      .in(file("rest_scala_core"))
      .settings(
        ( buildSettings ++ Seq(
          name := "REST Scala Core",
          version := restScalaDriverVersion,
          apiURL := Some(url(s"$apiRoot/$githubName/$docVersion/")),
          autoAPIMappings := true,
          libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaBuildVersion,
          libraryDependencies += "com.lihaoyi" %%% "utest" % utestJvmVersion % "test", //TODO
          testFrameworks += new TestFramework("utest.runner.Framework")
        )): _*)
      .jvmSettings()
      .jsSettings()

  lazy val rest_scala_core_JVM = rest_scala_core.jvm
  lazy val rest_scala_core_JS = rest_scala_core.js

//  lazy val rest_scala_core: Project = Project(
//    "rest_scala_core",
//    file("rest_scala_core"),
//    settings = buildSettings ++ Seq(
//      name := "REST Scala Core",
//      version := restScalaDriverVersion,
//      apiURL := Some(url(s"$apiRoot/$githubName/$docVersion/")),
//      autoAPIMappings := true,
//      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaBuildVersion,
//      libraryDependencies += utestJvmDeps,
//      testFrameworks += new TestFramework("utest.runner.Framework")
//    )
//  )

  lazy val rest_json_circe_module: Project = Project(
    "rest_json_circe_module",
    file("rest_json_circe_module"),
    settings = buildSettings ++ Seq(
      name := "REST JSON - CIRCE module",
      version := restScalaDriverVersion,
      apiURL := Some(url(s"$apiRoot/$githubName/$docVersion/")),
      autoAPIMappings := true,
      libraryDependencies += utestJvmDeps,
      libraryDependencies ++= circeDeps,
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
  ).dependsOn(rest_scala_core_JVM)

  // Doc project
  // (from https://groups.google.com/forum/#!topic/simple-build-tool/QXFsjLozLyU)
  def mainDirs(project: Project) = unmanagedSourceDirectories in project in Compile
  lazy val doc = Project("doc", file("doc"))
    .dependsOn(rest_scala_core_JVM, rest_json_circe_module)
    .settings(buildSettings ++ Seq(
        version := restScalaDriverVersion,
        unmanagedSourceDirectories in Compile <<= Seq(
          mainDirs(rest_scala_core_JVM),
          mainDirs(rest_json_circe_module)
        ).join.apply {(s) => s.flatten}
      )
    )
}
