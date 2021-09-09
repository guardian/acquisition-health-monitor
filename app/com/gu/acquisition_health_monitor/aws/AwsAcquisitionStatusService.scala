package com.gu.acquisition_health_monitor.aws

import _root_.com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusError, AcquisitionStatusService, AcquisitionStatusSuccess}
import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricDimensionName, MetricDimensionValue, MetricName, MetricNamespace, MetricPeriod, MetricRequest, MetricStats}

object AwsAcquisitionStatusService extends AcquisitionStatusService {
  override def getAcquisitionNumber: Map[String, AcquisitionStatus] = {

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

    val result = AwsCloudWatch.metricGet(request, None)

    val acquisitionStatus = result match {
      case Left(error) => AcquisitionStatusError(error)
      case Right(dataPoints) => AcquisitionStatusSuccess(dataPoints.values.sum.toInt)
    }

    Map[String, AcquisitionStatus]("Contribution" ->  acquisitionStatus)
  }
}
