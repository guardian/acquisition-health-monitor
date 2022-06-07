package com.gu.acquisition_health_monitor

object MultiPageResultFetcher {

  /*
  this function
  keeps calling getResponse with the next token until extractToken returns None
   */
  def fetchAllPages[RESPONSE](
    getResponse: Option[String] => Either[String, RESPONSE], // pass in None to get the first page
    extractToken: RESPONSE => Option[String] // return None to mean "no more pages"
  ): Either[String, List[RESPONSE]] = {

    def fetchAllPages0(
      token: Option[String],
      soFar: List[RESPONSE]
    ): Either[String, List[RESPONSE]] =
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
