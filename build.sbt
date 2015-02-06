name := """rad-translate-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies ++= Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.0" classifier "models",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.0" classifier "sources",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.0" classifier "javadoc",
  "org.apache.tika" % "tika-parsers" % "1.7"
)
