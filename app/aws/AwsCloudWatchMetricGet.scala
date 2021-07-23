package aws

import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.regions.Region.EU_WEST_1
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{Dimension, GetMetricDataRequest, Metric, MetricDataQuery, MetricDataResult, MetricDatum, MetricStat, PutMetricDataRequest, StandardUnit}

import scala.jdk.CollectionConverters._
import java.time.Instant
import scala.util.{Failure, Success, Try}

object Aws {
  //val ProfileName = "developerPlayground"
  val ProfileName = "mobile"

  lazy val CredentialsProvider: AwsCredentialsProviderChain = AwsCredentialsProviderChain
    .builder
    .credentialsProviders(
      ProfileCredentialsProvider.builder.profileName(ProfileName).build(),
      EnvironmentVariableCredentialsProvider.create()
    )
    .build()

}

object AwsCloudWatch {
  val client: CloudWatchClient = CloudWatchClient
    .builder
    .region(EU_WEST_1)
    .credentialsProvider(Aws.CredentialsProvider)
    .build()

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

  def metricGet(request: MetricRequest, nextToken: Option[String]): Unit = {

    val metricDataRequest: GetMetricDataRequest = buildMetricRequest(request, nextToken)

    val result = Try {
      client.getMetricData(metricDataRequest)
    }

    result match {
      case Success(value) => {
        Option(value.nextToken) match {
          case Some(next) => {
            metricGet(request, Some(next))
          }
          case None => {
            val metricResults = value.metricDataResults()
            if (metricResults.size > 0) {
              val test: MetricDataResult = metricResults.get(0)
              println("The label is " + test.label())
              println("The status code is " + test.statusCode().toString())
            }
          }
        }
      }
      case Failure(exception) => {
        println(exception.getMessage)
      }
    }
  }

  private[aws] def buildMetricRequest(request: MetricRequest, nextToken: Option[String]): GetMetricDataRequest = {

    val start = Instant.now.minusSeconds(18000)
    val endDate = Instant.now

    val query = MetricDataQuery.builder
      .metricStat(request.metricStat)
      .id("test1").returnData(false).build()

    GetMetricDataRequest.builder
      .maxDatapoints(100)
      .startTime(start)
      .endTime(endDate)
      .metricDataQueries(query)
      .nextToken(nextToken.orNull).build()
  }
}