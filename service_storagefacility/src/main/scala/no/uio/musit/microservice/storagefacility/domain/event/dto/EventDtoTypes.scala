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

package no.uio.musit.microservice.storagefacility.domain.event.dto

import java.sql.{Date => JSqlDate, Timestamp => JSqlTimestamp}

import no.uio.musit.microservice.storagefacility.domain.event.{EventId, EventTypeId}
import no.uio.musit.microservice.storagefacility.domain.MuseumId

// TODO: Change id and partOf to EventId

sealed trait EventDto {
  val id: Option[EventId]
  val eventTypeId: EventTypeId
  val eventDate: JSqlDate
  val relatedActors: Seq[EventRoleActor]
  val relatedObjects: Seq[EventRoleObject]
  val relatedPlaces: Seq[EventRolePlace]
  val note: Option[String]
  val relatedSubEvents: Seq[RelatedEvents]
  val partOf: Option[EventId]
  val valueLong: Option[Long]
  val valueString: Option[String]
  val valueDouble: Option[Double]
  val registeredBy: Option[String]
  val registeredDate: Option[JSqlTimestamp]
}

/**
 * The EventDto contains attributes that are common across _all_ event types.
 */
case class BaseEventDto(
  id: Option[EventId],
  eventTypeId: EventTypeId,
  eventDate: JSqlDate,
  relatedActors: Seq[EventRoleActor],
  relatedObjects: Seq[EventRoleObject],
  relatedPlaces: Seq[EventRolePlace],
  note: Option[String],
  relatedSubEvents: Seq[RelatedEvents],
  partOf: Option[EventId],
  valueLong: Option[Long] = None,
  valueString: Option[String] = None,
  valueDouble: Option[Double] = None,
  registeredBy: Option[String],
  registeredDate: Option[JSqlTimestamp]
) extends EventDto

sealed trait DtoExtension

/**
 * Having the ExtendedDto include the base EventDto allows for easier
 * conversions between domain and to.
 */
case class ExtendedDto(
    id: Option[EventId],
    eventTypeId: EventTypeId,
    eventDate: JSqlDate,
    relatedActors: Seq[EventRoleActor],
    relatedObjects: Seq[EventRoleObject],
    relatedPlaces: Seq[EventRolePlace],
    note: Option[String],
    relatedSubEvents: Seq[RelatedEvents],
    partOf: Option[EventId],
    valueLong: Option[Long] = None,
    valueString: Option[String] = None,
    valueDouble: Option[Double] = None,
    registeredBy: Option[String],
    registeredDate: Option[JSqlTimestamp],
    extension: DtoExtension
) extends EventDto {

  def baseEventDto: BaseEventDto = {
    BaseEventDto(
      id = id,
      eventTypeId = eventTypeId,
      eventDate = eventDate,
      relatedActors = relatedActors,
      relatedObjects = relatedObjects,
      relatedPlaces = relatedPlaces,
      note = note,
      relatedSubEvents = relatedSubEvents,
      partOf = partOf,
      valueLong = valueLong,
      valueString = valueString,
      valueDouble = valueDouble,
      registeredBy = registeredBy,
      registeredDate = registeredDate
    )
  }

}

object ExtendedDto {

  def apply(bed: BaseEventDto, ext: DtoExtension): ExtendedDto = {
    ExtendedDto(
      id = bed.id,
      eventTypeId = bed.eventTypeId,
      eventDate = bed.eventDate,
      relatedActors = bed.relatedActors,
      relatedObjects = bed.relatedObjects,
      relatedPlaces = bed.relatedPlaces,
      note = bed.note,
      relatedSubEvents = bed.relatedSubEvents,
      partOf = bed.partOf,
      valueLong = bed.valueLong,
      valueDouble = bed.valueDouble,
      valueString = bed.valueString,
      registeredBy = bed.registeredBy,
      registeredDate = bed.registeredDate,
      extension = ext
    )
  }

}

/**
 * Dto to handle environment requirements.
 */
case class EnvRequirementDto(
  id: Option[EventId],
  temperature: Option[Double],
  tempTolerance: Option[Int],
  airHumidity: Option[Double],
  airHumTolerance: Option[Int],
  hypoxicAir: Option[Double],
  hypoxicTolerance: Option[Int],
  cleaning: Option[String],
  light: Option[String]
) extends DtoExtension

/**
 * Dto that handles observation events with from and to attributes.
 */
case class ObservationFromToDto(
  id: Option[EventId],
  from: Option[Double],
  to: Option[Double]
) extends DtoExtension

/**
 * Dto to handle observation events related to pest control.
 */
case class ObservationPestDto(
  lifeCycles: Seq[LifecycleDto]
) extends DtoExtension

/**
 * Dto to handle pest life-cycles for an ObservationPestDto
 */
case class LifecycleDto(
  eventId: Option[EventId],
  stage: Option[String],
  quantity: Option[Int]
)

