lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion    = "2.6.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.labo_iot",
      scalaVersion    := "2.13.1"
    )),
    name := "labo-ambiante-mobile-backend-refacto",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.0.4",
      "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1",
      "ch.megard" %% "akka-http-cors" % "0.4.2",

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.0.8"         % Test
    )
  )
enablePlugins(JavaServerAppPackaging)
enablePlugins(AshScriptPlugin)
packageName in Docker := packageName.value
dockerBaseImage :="openjdk:jre-alpine"