package com.gu.acquisition_health_monitor

import controllers.{HomeController}
import com.gu.conf.{ConfigurationLoader, FileConfigurationLocation, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity, DevIdentity}
import play.api.ApplicationLoader.Context
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator}
import play.controllers.AssetsComponents
import play.filters.HttpFiltersComponents

import java.io.File

class AcquisitionApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {

    LoggerConfigurator(context.environment.classLoader) foreach { _.configure(context.environment) }

    val identity = AppIdentity.whoAmI(defaultAppName = "myApp")
    val loadedConfig = ConfigurationLoader.load(identity) {
      case AwsIdentity(app, stack, stage, _) =>
        SSMConfigurationLocation(s"/acquisition/$stage/$stack")

      case DevIdentity(_) =>
        val home = System.getProperty("user.home")
        FileConfigurationLocation(new File(s"/${home}/.gu/acquisition-health.monitor.conf"))
    }

    val newContext = context.copy(initialConfiguration = context.initialConfiguration ++ Configuration(loadedConfig))
    (new MyComponents(newContext)).application

  }
}

class MyComponents(context: Context) extends BuiltInComponentsFromContext(context) with HttpFiltersComponents with _root_.controllers.AssetsComponents{
  val homeController = new HomeController(controllerComponents)
  val assetController = new _root_.controllers.Assets(httpErrorHandler, assetsMetadata)
  lazy val router = new _root_.router.Routes(httpErrorHandler, homeController, assetController)
}