package com.gu.acquisition_health_monitor

object MultiPageResultFetcher {

  /*
  this function
  keeps calling getResponse with the next token until getNextTokenFromR returns None
   */
  def fetchAllPages[R](
    getResponse: Option[String] => Either[String, R], // pass in None to get the first page
    extractToken: R => Option[String] // return None to mean "no more pages"
  ): Either[String, List[R]] = {

    def fetchAllPages0(
      token: Option[String],
      soFar: List[R]
    ): Either[String, List[R]] =
      for {
        value <- getResponse(token)
        metricResults <- extractToken(value) match {
          case Some(next) =>
            fetchAllPages0(Some(next), value :: soFar)
          case None => // None means last page
            Right((value :: soFar).reverse)
        }
      } yield metricResults

    fetchAllPages0(None, Nil) // None means "first page"
  }

}
