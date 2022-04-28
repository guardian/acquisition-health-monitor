package com.gu.acquisition_health_monitor

object MultiPageResultFetcher {

  // in the test code:
  case class TestableNext(nextToken: String)

  /*
  this function
  keeps calling getResponse with the next token until getNextTokenFromR returns None
   */
  def fetchAllPages[R](
    getResponse: Option[String] => Either[String, R],
    token: Option[String],
    soFar: List[R],
    getNextTokenFromR: R => Option[String]
  ): Either[String, List[R]] = {

    for {
      value <- getResponse(token)
      metricResults <- getNextTokenFromR(value) match {
        case Some(next) =>
          fetchAllPages(getResponse, Some(next), value :: soFar, getNextTokenFromR)
        case None =>
          Right((value :: soFar).reverse)
      }
    } yield metricResults
  }

}
