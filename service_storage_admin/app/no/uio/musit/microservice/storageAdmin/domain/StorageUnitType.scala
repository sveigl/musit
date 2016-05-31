package no.uio.musit.microservice.storageAdmin.domain

import play.api.libs.json.{ Format, Json }

/**
 * Created by ellenjo on 5/19/16.
 */
sealed trait StorageUnitType

object StorageUnitType {
  def apply(stType: String) = stType.toLowerCase match {
    case "building" => Building
    case "room" => Room
    case "storageunit" => StUnit
    case other => throw new Exception(s"Musit: Undefined StorageType:$other")
  }

}

object Room extends StorageUnitType

object Building extends StorageUnitType

object StUnit extends StorageUnitType