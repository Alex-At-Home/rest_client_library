import sbt._
import Keys._
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
  val utestJvmVersion = "0.4.3"
  val rosHttpVersion = "2.0.0-RC1"

  // Project definitions

  val restScalaDriverVersion = "0.1-SNAPSHOT"

  lazy val root = Project(
    "root",
    file("."),
    settings = buildSettings
  )
  .enablePlugins(ScalaJSPlugin)
  .aggregate(
    rest_scala_coreJVM,
    rest_scala_coreJS,
    rest_json_circe_moduleJVM,
    rest_json_circe_moduleJS
  )

  val githubName = "rest_client_library"
  val apiRoot = "https://alex-at-home.github.io"
  val docVersion = "current"

  lazy val rest_scala_core = crossProject
      .in(file("rest_scala_core"))
      .settings(
        buildSettings ++ Seq(
          name := "REST Scala Core",
          version := restScalaDriverVersion,
          apiURL := Some(url(s"$apiRoot/$githubName/$docVersion/")),
          autoAPIMappings := true,
          libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaBuildVersion,
          libraryDependencies += "com.lihaoyi" %%% "utest" % utestJvmVersion % "test",
          testFrameworks += new TestFramework("utest.runner.Framework")
        ): _*)
      .jvmSettings()
      .jsSettings(
        scalaJSUseRhino in Global := false
      )

  lazy val rest_scala_coreJVM = rest_scala_core.jvm
  lazy val rest_scala_coreJS = rest_scala_core.js

  lazy val rest_json_circe_module = crossProject
    .in(file("rest_json_circe_module"))
    .settings(
      buildSettings ++ Seq(
        name := "REST JSON - CIRCE module",
        version := restScalaDriverVersion,
        apiURL := Some(url(s"$apiRoot/$githubName/$docVersion/")),
        autoAPIMappings := true,
        libraryDependencies += "com.lihaoyi" %%% "utest" % utestJvmVersion % "test",
        libraryDependencies ++= Seq(
            "io.circe" %%% "circe-core",
            "io.circe" %%% "circe-generic",
            "io.circe" %%% "circe-parser"
          )
          .map(_ % circeVersion),
        testFrameworks += new TestFramework("utest.runner.Framework")
      ): _*)
    .jvmSettings()
    .jsSettings(
      scalaJSUseRhino in Global := false
    )

  lazy val rest_json_circe_moduleJVM = rest_json_circe_module.jvm dependsOn rest_scala_coreJVM
  lazy val rest_json_circe_moduleJS = rest_json_circe_module.js dependsOn rest_scala_coreJS

  lazy val rest_json_js_moduleJS: Project = Project(
    "rest_json_js_module",
    file("rest_json_js_module"),
    settings = buildSettings ++ Seq(
      name := "REST JSON - ScalaJS module",
      version := restScalaDriverVersion,
      apiURL := Some(url(s"$apiRoot/$githubName/$docVersion/")),
      autoAPIMappings := true,
      libraryDependencies += "com.lihaoyi" %%% "utest" % utestJvmVersion % "test",
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
  ).enablePlugins(ScalaJSPlugin).dependsOn(rest_scala_coreJS)

  lazy val rest_http_client = crossProject
    .in(file("rest_http_client"))
    .settings(
      buildSettings ++ Seq(
        name := "REST HTTP Client",
        version := restScalaDriverVersion,
        apiURL := Some(url(s"$apiRoot/$githubName/$docVersion/")),
        autoAPIMappings := true,
        libraryDependencies += "fr.hmil" %%% "roshttp" % rosHttpVersion,
        libraryDependencies += "com.lihaoyi" %%% "utest" % utestJvmVersion % "test",
        testFrameworks += new TestFramework("utest.runner.Framework")
      ): _*)
    .jvmSettings()
    .jsSettings(
      scalaJSUseRhino in Global := false
    )

  lazy val rest_http_clientJVM = rest_http_client.jvm dependsOn rest_scala_coreJVM
  lazy val rest_http_clientJS = rest_http_client.js dependsOn rest_scala_coreJS

  // Doc project
  // (from https://groups.google.com/forum/#!topic/simple-build-tool/QXFsjLozLyU)
  def mainDirs(project: Project) = unmanagedSourceDirectories in project in Compile
  lazy val doc = Project("doc", file("doc"))
    .dependsOn(rest_scala_coreJVM, rest_json_circe_moduleJVM, rest_json_js_moduleJS, rest_http_clientJVM)
    .settings(buildSettings ++ Seq(
        version := restScalaDriverVersion,
        unmanagedSourceDirectories in Compile <<= Seq(
          mainDirs(rest_scala_coreJVM),
          mainDirs(rest_json_circe_moduleJVM),
          mainDirs(rest_json_js_moduleJS),
          mainDirs(rest_http_clientJVM)
        ).join.apply {(s) => s.flatten}
      )
    )
}
