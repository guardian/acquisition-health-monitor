import com.gu.{AppIdentity, AwsIdentity, DevIdentity}
import com.gu.conf.{ConfigurationLoader, FileConfigurationLocation, SSMConfigurationLocation}
import controllers.{Assets, AssetsComponents, HomeController}
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Configuration, LoggerConfigurator}
import play.api.ApplicationLoader.Context
import play.api.http.HttpErrorHandler
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes

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
    (new BuiltInComponentsFromContext(newContext) with MyComponents).application
  }
}

trait MyComponents extends BuiltInComponentsFromContext with HttpFiltersComponents with AssetsComponents{
  val homeController = new HomeController(controllerComponents)
  val assetController = new controllers.Assets(httpErrorHandler, assetsMetadata)
  lazy val router = new Routes(httpErrorHandler, homeController, assetController)
}