addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.11.0")
addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "1.1.9")
libraryDependencies += "org.vafer" % "jdeb" % "1.5" artifacts (Artifact("jdeb", "jar", "jar"))
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")

// needed for riffraff as it uses AWS S3 SDK 1.11 which uses some base64 methods that were removed in JDK11
// this is not needed for the current build server, but it is a good reason to move to AWS SDK v2
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"