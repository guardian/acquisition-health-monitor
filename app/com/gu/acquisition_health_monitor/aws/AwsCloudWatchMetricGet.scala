package com.gu.acquisition_health_monitor.aws

import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricRequest, MetricRequestBuilder}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.regions.Region.EU_WEST_1
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model._

import java.time.Instant
import scala.jdk.CollectionConverters._
import scala.util.Try



class AwsCloudWatch(credential:  AwsCredentialsProviderChain) {
  val client: CloudWatchClient = CloudWatchClient
    .builder
    .region(EU_WEST_1)
    .credentialsProvider(credential)
    .build()

  println(s"client: ${client}")

  private def listPaymentSuccessMetrics(): Either[String, ListMetricsResponse] = {
    val listMetricsRequest: ListMetricsRequest = ListMetricsRequest.builder()
      .namespace("support-frontend")
      .metricName("PaymentSuccess")
      .dimensions(DimensionFilter.builder.name("Stage").value("PROD").build())
      .build()
    Try {
      client.listMetrics(listMetricsRequest)
    }.toEither.left.map(x => x.toString)
  }

  def getAllPaymentSuccessMetrics(request: MetricRequest): Either[String, Map[String, Map[Instant, Double]]]= {
    for {
      listOfAllMetrics <- listPaymentSuccessMetrics()
      allMetrics <- getMetricDataFromMetrics(new MetricRequestBuilder(request, listOfAllMetrics), None, Nil)
    } yield groupMetricDataByProduct(allMetrics)
  }

  def getMetricDataFromMetrics(
    metricRequestBuilder: MetricRequestBuilder,
    nextToken: Option[String],
    soFar: List[GetMetricDataResponse]
  ): Either[String, List[GetMetricDataResponse]] = {

    val metricDataRequest: GetMetricDataRequest = metricRequestBuilder.build(nextToken)

    val failableResult = Try {
      client.getMetricData(metricDataRequest)
    }.toEither.left.map(x => x.toString)

    for {
      value <- failableResult
      metricResults <- Option(value.nextToken) match {
        case Some(next) =>
          getMetricDataFromMetrics(metricRequestBuilder, Some(next), value :: soFar)
        case None =>
         Right((value :: soFar).reverse)
      }
    } yield metricResults
  }

  private def groupMetricDataByProduct(responsePages: List[GetMetricDataResponse]) = {
    val pagesData: List[(String, Map[Instant, Double])] = for {
      page <- responsePages
      singleMetricDataForPage <- page.metricDataResults().asScala.toList
    } yield {
      println("value size: " + singleMetricDataForPage.values.size) // FIXME use the logger properly so it goes into the right place
      println("The label is " + singleMetricDataForPage.label())
      println("The status code is " + singleMetricDataForPage.statusCode().toString())
      val timestamps = singleMetricDataForPage.timestamps().asScala.toList
      val values = singleMetricDataForPage.values().asScala.toList.map(x => x.toDouble)
      val valuesForPage = timestamps.zip(values).toMap
      val labelForMetric = singleMetricDataForPage.label()
      (labelForMetric, valuesForPage)
    }
    // at this point, we have one record for each page and each metric.  So for 2 pages and 4 metrics we would have
    // 8 records.
    // next we need to merge together the data with the same label
    pagesData.groupMapReduce(_._1)(_._2)(_ concat _) // TODO might we get duplicate "Instant"s? check AWS docs
  }

}

object AwsCloudWatch {

  case class MetricNamespace(value: String) extends AnyVal

  case class MetricName(value: String) extends AnyVal

  case class MetricDimensionName(value: String) extends AnyVal

  case class MetricDimensionValue(value: String) extends AnyVal

  case class MetricStats(value: String) extends AnyVal

  case class MetricPeriod(value: Int) extends AnyVal

  case class MetricRequest(
    period: MetricPeriod,
    stat: MetricStats,
    start: Instant,
    endDate: Instant,
  )

  private def getLabelFromDimension(id: Int, dimensions: List[Dimension]) = {

    val label1 = dimensions.find(x => x.name() == "ProductType").map(_.value()).getOrElse(id + "-MissingProductType")
    val label2 = dimensions.find(x => x.name() == "PaymentProvider").map(_.value()).getOrElse(id + "-MissingPaymentProvider")

    label1 + "-" + label2
  }

  private class MetricRequestBuilder(request: MetricRequest, listMetricResponse: ListMetricsResponse) {

    def build(nextToken: Option[String]): GetMetricDataRequest = {

      val queries = listMetricResponse.metrics().asScala.zipWithIndex.map {
        case (metric, index) => {
          val metricStat = MetricStat.builder
            .stat(request.stat.value)
            .period(request.period.value)
            .metric(metric)
            .build()

          MetricDataQuery.builder
            .metricStat(metricStat)
            .id("id" + index)
            .label(getLabelFromDimension(index, metric.dimensions().asScala.toList))
            .returnData(true).build()
        }
      }

      GetMetricDataRequest.builder
        //.maxDatapoints(200)
        .startTime(request.start)
        .endTime(request.endDate)
        .metricDataQueries(queries.asJava)
        .nextToken(nextToken.orNull)
        .build()
    }
  }
}
