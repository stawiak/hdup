name := "hdup"

version := "1.0"

scalaVersion := "2.10.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.5")

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.1.0",
    "com.typesafe.akka" %% "akka-agent" % "2.1.0"
    )

libraryDependencies ++= Seq(
    "io.spray" % "spray-can" % "1.1-M7",
    "io.spray" % "spray-routing" % "1.1-M7"
    )


libraryDependencies += "com.typesafe.akka" % "akka-camel_2.10.0-RC2" % "2.1.0-RC2"

libraryDependencies += "org.apache.camel" % "camel-jetty" % "2.10.0"

libraryDependencies += "org.apache.activemq" % "activemq-camel" % "5.7.0"

libraryDependencies += "org.apache.activemq" % "activemq-core" % "5.7.0"


libraryDependencies += "org.apache.hadoop" % "hadoop-core" % "1.0.4"

libraryDependencies += "org.apache.hbase" % "hbase" % "0.92.1"