package com.gu.acquisition_health_monitor.aws

import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricDimensionName, MetricDimensionValue, MetricName, MetricNamespace, MetricPeriod, MetricRequest, MetricStats, buildMetricRequest}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.regions.Region.EU_WEST_1
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{Dimension, GetMetricDataRequest, Metric, MetricDataQuery, MetricDataResult, MetricDatum, MetricStat, PutMetricDataRequest, StandardUnit}

import scala.jdk.CollectionConverters._
import java.time.Instant
import scala.collection.View.Empty
import scala.util.{Failure, Success, Try}

object Aws {
  //val ProfileName = "developerPlayground"
  val ProfileName = "membership"

  lazy val CredentialsProvider: AwsCredentialsProviderChain = AwsCredentialsProviderChain
    .builder
    .credentialsProviders(
      ProfileCredentialsProvider.builder.profileName(ProfileName).build(),
      EnvironmentVariableCredentialsProvider.create()
    )
    .build()
}

class AwsCloudWatch(credential:  AwsCredentialsProviderChain) {
  val client: CloudWatchClient = CloudWatchClient
    .builder
    .region(EU_WEST_1)
    .credentialsProvider(credential)
    .build()

  println(s"client: ${client}")
  def metricGet(request: MetricRequest, nextToken: Option[String]): Either[String, Map[Instant, Double]] = {

    val metricDataRequest: GetMetricDataRequest = buildMetricRequest(request, nextToken)

    val failableResult = Try {
      client.getMetricData(metricDataRequest)
    }.toEither.left.map(x => x.toString)

    for {
      value <- failableResult
      metricResults <-  Option(value.nextToken) match {
        case Some(next) => {
          metricGet(request, Some(next))
        }
        case None => {
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

          results.headOption.toRight("Did not get any result back from AWS")
        }
      }
    } yield {
      metricResults
    }
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
                            namespace: MetricNamespace,
                            name: MetricName,
                            dimensions: Map[MetricDimensionName, MetricDimensionValue],
                            period: MetricPeriod,
                            stat: MetricStats
                          ){

  val metricDimensions = dimensions.map {
    case (name, value) =>
      Dimension.builder.name(name.value).value(value.value).build()
  }.toList.asJava

  val metric = Metric.builder
    .metricName(name.value)
    .namespace(namespace.value)
    .dimensions(metricDimensions)
    .build()

  val metricStat = MetricStat.builder
    .stat(stat.value)
    .period(period.value)
    .metric(metric)
    .build()
  }

  private[aws] def buildMetricRequest(request: MetricRequest, nextToken: Option[String]): GetMetricDataRequest = {

    //println("Now: " + Instant.now())
    val start = Instant.parse("2021-08-31T15:00:00Z")
    val endDate = Instant.parse("2021-08-31T16:00:00Z")

    val query = MetricDataQuery.builder
      .metricStat(request.metricStat)
      .id("myRequest")
      .label("myRequestLabel")
      .returnData(true).build()

    GetMetricDataRequest.builder
      //.maxDatapoints(200)
      .startTime(start)
      .endTime(endDate)
      .metricDataQueries(query)
      .nextToken(nextToken.orNull)
      .build()
  }
}