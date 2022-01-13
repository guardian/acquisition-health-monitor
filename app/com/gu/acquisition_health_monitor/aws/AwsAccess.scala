package com.gu.acquisition_health_monitor.aws

import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

object AwsAccess {
  val ProfileName = "membership"

  lazy val membershipLocal: AwsCredentialsProviderChain = AwsCredentialsProviderChain
    .builder
    .credentialsProviders(
      ProfileCredentialsProvider.builder.profileName(ProfileName).build(),
      EnvironmentVariableCredentialsProvider.create()
    )
    .build()

  def assumeRoleForAws(roleArn: String) = {
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
}

object AwsAccessThroughAssumeRole {


}