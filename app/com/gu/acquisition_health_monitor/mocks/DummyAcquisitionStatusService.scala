package com.gu.acquisition_health_monitor.mocks

import com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusService, AcquisitionStatusSuccess}

object DummyAcquisitionStatusService extends AcquisitionStatusService {
  override def getAcquisitionNumber: Map[String, AcquisitionStatus] = {
    val dummyAcquisitionStatus = AcquisitionStatusSuccess(22)

    Map("acquisition1" -> dummyAcquisitionStatus)
  }
}
