import controllers.{Assets, AssetsComponents, HomeController}
import play.api.{ApplicationLoader, BuiltInComponentsFromContext}
import play.api.ApplicationLoader.Context
import play.api.http.HttpErrorHandler
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import router.Routes

class AcquisitionApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    new MyComponents(context).application
  }
}

class MyComponents(context: Context) extends BuiltInComponentsFromContext(context) with HttpFiltersComponents with AssetsComponents{
  val homeController = new HomeController(controllerComponents)
  val assetController = new controllers.Assets(httpErrorHandler, assetsMetadata)
  lazy val router = new Routes(httpErrorHandler, homeController, assetController)
}