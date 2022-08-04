name := """acquisition-health-monitor"""
organization := "com.gu"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"
val circeVersion = "0.14.2"

// AWS
val awsSdkVersion = "2.17.229"
val awsCloudwatch = "software.amazon.awssdk" % "cloudwatch" % awsSdkVersion
val awsSts = "software.amazon.awssdk" % "sts" % awsSdkVersion

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  ws,
  "com.gu" %% "simple-configuration-ssm" % "1.5.7",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.6",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.6",
//  "com.dripower" %% "play-circe" % "2814.1",
  awsCloudwatch,
  awsSts,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
)

enablePlugins(SystemdPlugin, PlayScala, RiffRaffArtifact, JDebPackaging)
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), "cfn/cfn.yaml")
riffRaffManifestProjectName := s"playground::${name.value}"
riffRaffPackageType := (Debian / packageBin).value
Debian / packageName := name.value
