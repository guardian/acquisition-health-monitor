package com.gu.acquisition_health_monitor.aws

import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricDimensionName, MetricDimensionValue, MetricName, MetricNamespace, MetricPeriod, MetricRequest, MetricStats, buildMetricRequest}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.regions.Region.EU_WEST_1
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{Dimension, DimensionFilter, GetMetricDataRequest, GetMetricDataResponse, GetMetricStatisticsRequest, ListMetricsRequest, ListMetricsResponse, Metric, MetricDataQuery, MetricDataResult, MetricDatum, MetricStat, PutMetricDataRequest, StandardUnit}

import scala.jdk.CollectionConverters._
import java.time.Instant
import scala.collection.View.Empty
import scala.util.{Failure, Success, Try}



class AwsCloudWatch(credential:  AwsCredentialsProviderChain) {
  val client: CloudWatchClient = CloudWatchClient
    .builder
    .region(EU_WEST_1)
    .credentialsProvider(credential)
    .build()

  println(s"client: ${client}")

  private def listMetrics(): Either[String, ListMetricsResponse] = {
    val listMetricsRequest: ListMetricsRequest = ListMetricsRequest.builder()
      .namespace("support-frontend")
      .metricName("PaymentSuccess")
      .dimensions(DimensionFilter.builder.name("Stage").value("PROD").build())
      .build();
    val failableResult = Try {
      client.listMetrics(listMetricsRequest)
    }.toEither.left.map(x => x.toString)

    failableResult
  }

  def getAllMetrics(request: MetricRequest): Either[String, List[Map[Instant, Double]]]= {
    for {
      listOfAllMetrics <- listMetrics()
      allMetrics <- metricGet(request, None, listOfAllMetrics)
    } yield AwsToScala(allMetrics)
  }

  def metricGet(request: MetricRequest, nextToken: Option[String], listMetricsResponse: ListMetricsResponse): Either[String, GetMetricDataResponse] = {
    val metricDataRequest: GetMetricDataRequest = buildMetricRequest(request, listMetricsResponse, nextToken)

    val failableResult = Try {
      client.getMetricData(metricDataRequest)
    }.toEither.left.map(x => x.toString)

    for {
      value <- failableResult
      metricResults <-  Option(value.nextToken) match {
        case Some(next) => {
          metricGet(request, Some(next), listMetricsResponse)
        }
        case None => {
         Right(value)
        }
      }
    } yield {
      metricResults
    }
  }

  private def AwsToScala(value: GetMetricDataResponse) = {
    val metricResults = value.metricDataResults().asScala.toList
    val results = metricResults.map {
      metricResult => {
        println("value size: " + metricResult.values.size)
        println("The label is " + metricResult.label())
        println("The status code is " + metricResult.statusCode().toString())
        val timestamps = metricResult.timestamps().asScala.toList
        val values = metricResult.values().asScala.toList.map(x => x.toDouble)
        timestamps.zip(values).toMap
      }
    }

    results
  }
}

object AwsCloudWatch {

  case class MetricNamespace(value: String) extends AnyVal

  case class MetricName(value: String) extends AnyVal

  case class MetricDimensionName(value: String) extends AnyVal

  case class MetricDimensionValue(value: String) extends AnyVal

  case class MetricStats(value: String) extends AnyVal

  case class MetricPeriod(value: Int) extends AnyVal

  case class MetricRequest(period: MetricPeriod, stat: MetricStats)

  private def getLabelFromDimension(id: Int, dimensions: List[Dimension]) = {

    val label1 = dimensions.find(x => x.name() == "ProductType").map(_.value()).getOrElse(id + "-MissingProductType")
    val label2 = dimensions.find(x => x.name() == "PaymentProvider").map(_.value()).getOrElse(id + "-MissingPaymentProvider")

    label1 + "-" + label2
  }

  private[aws] def buildMetricRequest(request: MetricRequest, listMetricResponse: ListMetricsResponse, nextToken: Option[String]): GetMetricDataRequest = {

    //println("Now: " + Instant.now())
    val start = Instant.parse("2022-01-13T10:00:00Z")
    val endDate = Instant.parse("2022-01-13T16:00:00Z")


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
      .startTime(start)
      .endTime(endDate)
      .metricDataQueries(queries.asJava)
      .nextToken(nextToken.orNull)
      .build()
  }
}