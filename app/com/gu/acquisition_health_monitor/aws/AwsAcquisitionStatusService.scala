package com.gu.acquisition_health_monitor.aws

import _root_.com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusError, AcquisitionStatusService, AcquisitionStatusSuccess}
import com.gu.acquisition_health_monitor.aws.AwsAccess._
import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricPeriod, MetricRequest, MetricStats}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import java.time.Instant

class AwsAcquisitionStatusService(assumeRoleArn: Option[String]) extends AcquisitionStatusService {
  def getCredentialFromAssumeRole: AwsCredentialsProviderChain =
    assumeRoleArn.map(assumeRoleForAws).getOrElse(membershipLocal)

  override def getAcquisitionNumber: AcquisitionStatus = {
    val request = MetricRequest(
      MetricPeriod(60),
      MetricStats("Sum"),
      start = Instant.parse("2022-03-03T10:00:00Z"),
      endDate = Instant.parse("2022-03-03T16:00:00Z")
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
