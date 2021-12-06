package com.gu.acquisition_health_monitor

import com.gu.acquisition_health_monitor.aws.AwsAcquisitionStatusService
import controllers.HomeController
import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity, DevIdentity}
import play.api.ApplicationLoader.Context
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator, Mode}
import play.filters.HttpFiltersComponents
import software.amazon.awssdk.auth.credentials.{InstanceProfileCredentialsProvider, ProfileCredentialsProvider}
import scala.util.Success

class AcquisitionApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {

    LoggerConfigurator(context.environment.classLoader) foreach { _.configure(context.environment) }
    val isDev = context.environment.mode == Mode.Dev

    val (identity, credentialsProvider) = if (isDev)
      (
        Success(DevIdentity("developerPlayground")),
        ProfileCredentialsProvider.builder().profileName("developerPlayground").build()
      )
    else {
      val provider = InstanceProfileCredentialsProvider.builder().build()
      (
        AppIdentity.whoAmI(defaultAppName = "myApp", provider),
        provider
      )
    }

    val loadedConfig = ConfigurationLoader.load(identity.get, credentialsProvider) {
      case DevIdentity(_) => SSMConfigurationLocation(s"/acquisition/DEV/playground", "eu-west-1")
      case AwsIdentity(app, stack, stage, _) => SSMConfigurationLocation(s"/acquisition/$stage/$stack", "eu-west-1")
    }

    val newContext = context.copy(initialConfiguration = context.initialConfiguration ++ Configuration(loadedConfig))
    (new MyComponents(newContext)).application

  }
}

class MyComponents(context: Context) extends BuiltInComponentsFromContext(context) with HttpFiltersComponents with _root_.controllers.AssetsComponents{
  val assumeRole = context.initialConfiguration.getOptional[String]("MembershipAccessRoleArn")
  println(s"assume role: ${assumeRole}")
  val awsAcquisitionStatusService: AwsAcquisitionStatusService = new AwsAcquisitionStatusService(assumeRole)
  val homeController = new HomeController(controllerComponents, awsAcquisitionStatusService)
  val assetController = new _root_.controllers.Assets(httpErrorHandler, assetsMetadata)
  lazy val router = new _root_.router.Routes(httpErrorHandler, homeController, assetController)
}