package com.gu.acquisition_health_monitor.aws

import _root_.com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusError, AcquisitionStatusService, AcquisitionStatusSuccess}
import com.gu.acquisition_health_monitor.aws.AwsAccess._
import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricPeriod, MetricRequest, MetricStats}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain

class AwsAcquisitionStatusService(assumeRoleArn: Option[String]) extends AcquisitionStatusService {
  def getCredentialFromAssumeRole: AwsCredentialsProviderChain =
    assumeRoleArn.map(assumeRoleForAws).getOrElse(membershipLocal)

  override def getAcquisitionNumber: Map[String, AcquisitionStatus] = {
    val request = MetricRequest(
      MetricPeriod(60),
      MetricStats("Sum")
    )

    val result = new AwsCloudWatch(getCredentialFromAssumeRole).getAllMetrics(request)

    val acquisitionStatus = result match {
      case Left(error) => AcquisitionStatusError(error)
      case Right(dataPoints) => AcquisitionStatusSuccess(dataPoints.head.values.sum.toInt)//TODO
    }

    Map[String, AcquisitionStatus]("Contribution" ->  acquisitionStatus)
  }
}
