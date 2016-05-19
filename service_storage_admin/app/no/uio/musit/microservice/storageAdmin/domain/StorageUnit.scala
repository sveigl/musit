/*
 * MUSIT is a museum database to archive natural and cultural history data.
 * Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package no.uio.musit.microservice.storageAdmin.domain

import no.uio.musit.microservices.common.domain.BaseMusitDomain
import no.uio.musit.microservices.common.linking.domain.Link
import play.api.libs.json.Json


case class StorageUnit(id: Long, storageUnitName:String,storageType:String,links: Seq[Link]) extends BaseMusitDomain

object StorageUnit {
  def tupled = (StorageUnit.apply _).tupled
  implicit val format = Json.format[StorageUnit]
}

case class Room(id: Long, sikringSkallsikring :String, sikringTyverisikring: String,
sikringBrannsikring:String, sikringVannskaderisiko:String, sikringRutineOgBeredskap:String,
bevarLuftfuktOgTemp: String, bevarLysforhold: String, bevarPrevantKons: String,links: Seq[Link]) extends BaseMusitDomain

object Room {
  def tupled = (Room.apply _).tupled
  implicit val format = Json.format[Room]
}

case class Building(id: Long , address: String, links: Seq[Link]) extends BaseMusitDomain

object Building {
  def tupled = (Building.apply _).tupled
  implicit val format = Json.format[Building]
}

case class STORAGE_UNIT_LINK(id: Long,storage_unit_id: Long , relation: String, links: Seq[Link]) extends BaseMusitDomain

object STORAGE_UNIT_LINK {
  def tupled = (STORAGE_UNIT_LINK.apply _).tupled
  implicit val format = Json.format[STORAGE_UNIT_LINK]
}

