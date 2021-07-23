package controllers

import aws.AwsCloudWatch
import aws.AwsCloudWatch._
import play.api.mvc._

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] => {

    println("hello")
      val res = AwsCloudWatch.metricGet(MetricRequest(
        MetricNamespace("Mapi/PROD/mobile-fronts"),
        MetricName("ophan-api-success"),
        Map(
          //MetricDimensionName("Stage") -> MetricDimensionValue("PROD")
        ),
        MetricPeriod(60),
        MetricStats("Minimum")
      ), None)

      Ok(views.html.index())
    }
  }

  def healthCheck() = Action { implicit request: Request[AnyContent] =>
    Ok("OK")
  }
}
