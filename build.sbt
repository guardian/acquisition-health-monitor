name := """acquisition-health-monitor"""
organization := "com.gu"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += ws
libraryDependencies += "com.gu" %% "simple-configuration-ssm" % "1.5.7"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.6"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.6"

enablePlugins(SystemdPlugin, PlayScala, RiffRaffArtifact, JDebPackaging)
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), "cfn/cfn.yaml")
riffRaffManifestProjectName := s"playground::${name.value}"
riffRaffPackageType := (packageBin in Debian).value
packageName in Debian := name.value

// AWS
val awsSdkVersion = "2.16.100"
val awsCloudwatch = "software.amazon.awssdk" % "cloudwatch" % awsSdkVersion
val awsSts = "software.amazon.awssdk" % "sts" % awsSdkVersion

libraryDependencies ++= Seq(awsCloudwatch, awsSts)
