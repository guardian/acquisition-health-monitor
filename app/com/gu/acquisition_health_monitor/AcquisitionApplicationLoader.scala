package com.gu.acquisition_health_monitor

import com.gu.acquisition_health_monitor.aws.AwsAcquisitionStatusService
import controllers.HomeController
import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}
import com.gu.{AppIdentity}
import play.api.ApplicationLoader.Context
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator}
import play.filters.HttpFiltersComponents
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider

import java.io.File

class AcquisitionApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {

    LoggerConfigurator(context.environment.classLoader) foreach { _.configure(context.environment) }

    val identity = AppIdentity.whoAmI(defaultAppName = "myApp")
    val loadedConfig = ConfigurationLoader.load(identity) {
      case _ => SSMConfigurationLocation(s"/acquisition/CODE/playground")

//      case AwsIdentity(app, stack, stage, _) =>
//        SSMConfigurationLocation(s"/acquisition/$stage/$stack")
//
//      case DevIdentity(_) =>
//        val home = System.getProperty("user.home")
//        FileConfigurationLocation(new File(s"/${home}/.gu/acquisition-health.monitor.conf"))
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