package com.gu.acquisition_health_monitor

import org.scalatest._
import flatspec._
import matchers._

class MultiPageResultFetcherTest extends AnyFlatSpec with should.Matchers {

  behavior of "MultiPageResultFetcherTest"

  "fetchAllPages" should "give up after 1 page if None comes back from getNextToken" in {
    val result = MultiPageResultFetcher.fetchAllPages(
      getResponse = maybeString => Right("a result"),
      extractToken = (_: String) => None
    )

    result should be(Right(List("a result")))

  }

  it should "group a second page if it's available" in {
    val result = MultiPageResultFetcher.fetchAllPages[String](
      getResponse = {
        case None => Right("first page")
        case Some("token") => Right("second page")
      },
      extractToken = {
        case "first page" => Some("token")
        case "second page" => None
      }
    )

    result should be(Right(List("first page", "second page")))

  }

  it should "stop if a Left is returned" in {
    val result = MultiPageResultFetcher.fetchAllPages(
      getResponse = maybeString => Left("failure happened"),
      extractToken = (_: String) => None
    )

    result should be(Left("failure happened"))

  }

  it should "stop if the second page fails" in {
    val result = MultiPageResultFetcher.fetchAllPages[String](
      getResponse = {
        case None => Right("first page")
        case Some("token") => Left("second page failed")
      },
      extractToken = {
        case "first page" => Some("token")
      }
    )

    result should be(Left("second page failed"))

  }

}
