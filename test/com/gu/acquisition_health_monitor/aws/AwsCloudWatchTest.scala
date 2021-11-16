package com.gu.acquisition_health_monitor.aws

import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricDimensionName, MetricDimensionValue, MetricName, MetricNamespace, MetricPeriod, MetricRequest, MetricStats}
import org.scalatest.flatspec.AnyFlatSpec

class AwsCloudWatchTest extends AnyFlatSpec {

  behavior of "AwsCloudWatchTest"

  it should "metricGet" in {
    val request = MetricRequest(
      MetricNamespace("support-frontend"),
      MetricName("PaymentSuccess"),
      Map(
        MetricDimensionName("PaymentProvider") -> MetricDimensionValue("Stripe"),
        MetricDimensionName("ProductType") -> MetricDimensionValue("Contribution"),
        MetricDimensionName("Stage") -> MetricDimensionValue("PROD"),
      ),
      MetricPeriod(60),
      MetricStats("Average")
    )

    //val res = AwsCloudWatch.metricGet(request, None)
  }

}
