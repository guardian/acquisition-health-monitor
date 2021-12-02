package com.gu.acquisition_health_monitor.aws

import _root_.com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusError, AcquisitionStatusService, AcquisitionStatusSuccess}
import com.gu.acquisition_health_monitor.aws.AwsCloudWatch.{MetricDimensionName, MetricDimensionValue, MetricName, MetricNamespace, MetricPeriod, MetricRequest, MetricStats}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, AwsCredentialsProviderChain, ProfileCredentialsProvider}
import software.amazon.awssdk
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

class AwsAcquisitionStatusService(assumeRoleArn: Option[String]) extends AcquisitionStatusService {
  def getCredentialFromAssumeRole: AwsCredentialsProviderChain = {
      assumeRoleArn.map {
        roleArn => {
          AwsCredentialsProviderChain.builder().addCredentialsProvider(
            {
              val req: AssumeRoleRequest = AssumeRoleRequest.builder
                .roleArn(roleArn)
                .roleSessionName("testAR")
                .build()

              val stsClient: StsClient = StsClient.builder.build()

              StsAssumeRoleCredentialsProvider.builder
                .stsClient(stsClient)
                .refreshRequest(req)
                .build()
            }
          ).build()
      }
    }.getOrElse(Aws.CredentialsProvider)
  }

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

    val result = new AwsCloudWatch(getCredentialFromAssumeRole).metricGet(request, None)

    val acquisitionStatus = result match {
      case Left(error) => AcquisitionStatusError(error)
      case Right(dataPoints) => AcquisitionStatusSuccess(dataPoints.values.sum.toInt)
    }

    Map[String, AcquisitionStatus]("Contribution" ->  acquisitionStatus)
  }
}
