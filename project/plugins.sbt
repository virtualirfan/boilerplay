scalacOptions ++= Seq( "-unchecked", "-deprecation" )

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.9")

// SBT-Web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

// Scala.js
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.13")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.3.0" exclude("org.scala-js", "sbt-scalajs"))

// App Packaging
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

// Benchmarking
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.2.11")

addSbtPlugin("io.gatling" % "gatling-sbt" % "2.1.7")

// Dependency Resolution
//addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M14-3") // Causes AbstractMethodErrors

// Code Quality
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0") // scalastyle

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4") // scapegoat

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5") // stats

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2") // dependencyGraph

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10") // dependencyUpdates

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0") // scalariformFormat

addSbtPlugin("com.github.xuwei-k" % "sbt-class-diagram" % "0.1.7")
