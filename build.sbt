val scala2Version = "2.13.5"
val zioVersion = "1.0.9"

lazy val root = project
  .in(file("."))
  .settings(
    name := "explore-zio",
    version := "0.1.0",
    libraryDependencies += "dev.zio" %% "zio" % zioVersion,
    libraryDependencies += "dev.zio" %% "zio-streams" % zioVersion,
    libraryDependencies += "io.github.kitlangton" %% "zio-magic" % "0.3.2",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    scalaVersion := scala2Version
  )
