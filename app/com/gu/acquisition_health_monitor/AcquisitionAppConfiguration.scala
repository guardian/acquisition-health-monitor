package com.gu.acquisition_health_monitor

import com.gu.AppIdentity

object AcquisitionAppConfiguration {

  private lazy val identity = AppIdentity.whoAmI("acquisition-health-monitor-api")
  //  val config: Config = ConfigurationLoader.load(identity) {
  //    case identity: AwsIdentity => S3ConfigurationLocation.default(identity)
  //  }
}
