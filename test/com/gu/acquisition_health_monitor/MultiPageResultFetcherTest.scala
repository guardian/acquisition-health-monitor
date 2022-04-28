package com.gu.acquisition_health_monitor

import org.scalatest._
import flatspec._
import matchers._

class MultiPageResultFetcherTest extends AnyFlatSpec with should.Matchers {

  behavior of "MultiPageResultFetcherTest"

  "fetchAllPages" should "give up after 1 page if None comes back from getNextToken" in {
    val result = MultiPageResultFetcher.fetchAllPages(
      getResponse = maybeString => Right("a result"),
      token = None,
      soFar = Nil,
      getNextTokenFromR = (_: String) => None
    )

    result should be(Right(List("a result")))

  }

  "fetchAllPages" should "group a second page if it's available" in {
    val result = MultiPageResultFetcher.fetchAllPages[String](
      token => Right(token.get),
      Some("first"),
      Nil,
      {
        case "first" => Some("second")
        case _ => None
      }
    )

    result should be(Right(List("first", "second")))

  }

}
