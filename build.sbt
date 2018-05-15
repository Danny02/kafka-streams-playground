name := "kafka-stream"

version := "0.1"

scalaVersion := "2.12.5"

resolvers += Resolver.bintrayRepo("ovotech", "maven")
val kafkaSerializationV = "0.3.9"

libraryDependencies ++= Seq(
  "io.monix" %% "monix-kafka-1x" % "1.0.0-RC1",
  "com.lightbend" %% "kafka-streams-scala" % "0.2.1",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "com.ovoenergy" %% "kafka-serialization-core" % kafkaSerializationV,
  "com.ovoenergy" %% "kafka-serialization-circe" % kafkaSerializationV
)

val circeVersion = "0.9.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

fork in run := true