package com.gu.acquisition_health_monitor.controllers
import com.gu.acquisition_health_monitor.aws.AwsAcquisitionStatusService
import play.api.mvc._

import java.time.Instant
import java.time.format.DateTimeParseException
import javax.inject._
import scala.util.Try

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, awsAcquisitionStatusService: AwsAcquisitionStatusService) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index(start: String, end: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>

    val failableResult: Try[Result] = for {
      startDate <- Try(Instant.parse(start)) //"2022-03-03T10:00:00Z"
      endDate <- Try(Instant.parse(end)) //"2022-03-03T16:00:00Z"
    } yield {
      val response = awsAcquisitionStatusService.getAcquisitionNumber(startDate, endDate)
      Ok(response.toString()) // TODO change to JSON
    }
    failableResult.recover({
      case t: DateTimeParseException =>
        BadRequest("bad request, invalid time (TODO something useful)")
    }).get
  }

  def healthCheck() = Action { implicit request: Request[AnyContent] =>
    Ok("OK")
  }
}
