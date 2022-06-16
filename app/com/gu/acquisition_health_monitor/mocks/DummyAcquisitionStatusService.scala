package com.gu.acquisition_health_monitor.mocks

import com.gu.acquisition_health_monitor.{AcquisitionStatus, AcquisitionStatusService, AcquisitionStatusSuccess}

import java.time.Instant

object DummyAcquisitionStatusService extends AcquisitionStatusService {
  override def getAcquisitionNumber(startDate: Instant, endDate: Instant): AcquisitionStatus = AcquisitionStatusSuccess(Map("dummy" -> 22))

}
