name := "mattermost-scala"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
   "com.typesafe.akka" % "akka-actor_2.11" % "2.4.9-RC1",
   "com.typesafe.akka" %% "akka-http-core" % "2.4.9-RC1"
)

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.2"
libraryDependencies += "com.github.nscala-time" % "nscala-time_2.11" % "2.12.0"