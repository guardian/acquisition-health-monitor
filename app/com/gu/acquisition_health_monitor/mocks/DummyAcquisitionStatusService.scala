package com.gu.acquisition_health_monitor.mocks

import com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusService, AcquisitionStatusSuccess}

object DummyAcquisitionStatusService extends AcquisitionStatusService {
  override def getAcquisitionNumber: AcquisitionStatus = AcquisitionStatusSuccess(Map("dummy" -> 22))

}
