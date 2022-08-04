package com.gu.acquisition_health_monitor

import java.time.Instant


sealed trait AcquisitionStatus

case class AcquisitionStatusSuccess(numberOfRecentAcquisition: Map[String, Int]) extends AcquisitionStatus
case class AcquisitionStatusError(error: String) extends AcquisitionStatus

trait AcquisitionStatusService {
  // maybe using enum for string
  def getAcquisitionNumber(startDate: Instant, endDate: Instant): AcquisitionStatus
}
