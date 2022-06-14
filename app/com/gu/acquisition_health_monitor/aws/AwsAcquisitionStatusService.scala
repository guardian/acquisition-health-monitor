package com.gu.acquisition_health_monitor.aws

import _root_.com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusError, AcquisitionStatusService, AcquisitionStatusSuccess}
import com.gu.acquisition_health_monitor.aws.AwsAccess._
import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricPeriod, MetricRequest, MetricStats}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import java.time.Instant

class AwsAcquisitionStatusService(assumeRoleArn: Option[String]) extends AcquisitionStatusService {
  def getCredentialFromAssumeRole: AwsCredentialsProviderChain =
    assumeRoleArn.map(assumeRoleForAws).getOrElse(membershipLocal)

  override def getAcquisitionNumber(startDate: Instant, endDate: Instant): AcquisitionStatus = {
    val request = MetricRequest(
      MetricPeriod(60),
      MetricStats("Sum"),
      start = startDate,
      endDate = endDate
    )

    val result = new AwsCloudWatch(getCredentialFromAssumeRole).getAllPaymentSuccessMetrics(request)

    result match {
      case Left(error) => AcquisitionStatusError(error)
      case Right(metricsDataPerProduct) => AcquisitionStatusSuccess(metricsDataPerProduct.map { case (successLabel, dataPoints) =>
        successLabel -> dataPoints.values.sum.toInt
      })
    }

  }
}
