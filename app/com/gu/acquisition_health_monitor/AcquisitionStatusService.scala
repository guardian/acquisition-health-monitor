package com.gu.acquisition_health_monitor


sealed trait AcquisitionStatus

case class AcquisitionStatusSuccess(numberOfRecentAcquisition: Int) extends AcquisitionStatus
case class AcquisitionStatusError(error: String) extends AcquisitionStatus

trait AcquisitionStatusService {
  // maybe using enum for string
  def getAcquisitionNumber: Map[String, AcquisitionStatus]
}