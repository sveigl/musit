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

package no.uio.musit.microservice.storagefacility.service

import com.google.inject.Inject
import no.uio.musit.microservice.storagefacility.dao.event.EventDao
import no.uio.musit.microservice.storagefacility.domain.MuseumId
import no.uio.musit.microservice.storagefacility.domain.datetime.dateTimeNow
import no.uio.musit.microservice.storagefacility.domain.event.EventId
import no.uio.musit.microservice.storagefacility.domain.event.EventTypeRegistry.TopLevelEvents.ControlEventType
import no.uio.musit.microservice.storagefacility.domain.event.control.Control
import no.uio.musit.microservice.storagefacility.domain.event.dto.BaseEventDto
import no.uio.musit.microservice.storagefacility.domain.event.dto.DtoConverters.CtrlConverters
import no.uio.musit.microservice.storagefacility.domain.storage.StorageNodeId
import no.uio.musit.service.MusitResults._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class ControlService @Inject() (
    val eventDao: EventDao,
    val storageNodeService: StorageNodeService
) {

  val logger = Logger(classOf[ControlService])

  /**
   *
   */
  def add(
    mid: MuseumId,
    nodeId: StorageNodeId,
    ctrl: Control
  )(implicit currUsr: String): Future[MusitResult[Control]] = {
    storageNodeService.exists(mid, nodeId).flatMap {
      case MusitSuccess(nodeExists) =>
        if (nodeExists) {
          val c = ctrl.copy(
            affectedThing = Some(nodeId),
            registeredBy = Some(currUsr),
            registeredDate = Some(dateTimeNow)
          )
          val dto = CtrlConverters.controlToDto(c)
          eventDao.insertEvent(mid, dto).flatMap { eventId =>
            eventDao.getEvent(mid, eventId).map { res =>
              res.flatMap(_.map { dto =>
                // We know we have a BaseEventDto representing a Control.
                val bdto = dto.asInstanceOf[BaseEventDto]
                MusitSuccess(CtrlConverters.controlFromDto(bdto))
              }.getOrElse {
                logger.error(
                  s"An unexpected error occured when trying to fetch a " +
                    s"control event that was added with eventId $eventId"
                )
                MusitInternalError("Could not locate the control that was added")
              })
            }
          }
        } else {
          Future.successful(MusitValidationError("Node not found."))
        }

      case err: MusitError =>
        logger.error("An error occured when trying to add a Control")
        Future.successful(err)
    }
  }

  /**
   *
   * @param id
   * @return
   */
  def findBy(mid: MuseumId, id: EventId): Future[MusitResult[Option[Control]]] = {
    eventDao.getEvent(mid, id.underlying).map { result =>
      result.flatMap(_.map {
        case base: BaseEventDto =>
          MusitSuccess(
            Option(CtrlConverters.controlFromDto(base))
          )

        case _ =>
          MusitInternalError(
            "Unexpected DTO type. Expected BaseEventDto with event type Control"
          )
      }.getOrElse(MusitSuccess(None)))
    }
  }

  def listFor(mid: MuseumId, nodeId: StorageNodeId): Future[MusitResult[Seq[Control]]] = {
    eventDao.getEventsForNode(mid, nodeId, ControlEventType).map { dtos =>
      MusitSuccess(dtos.map { dto =>
        // We know we have a BaseEventDto representing a Control.
        val base = dto.asInstanceOf[BaseEventDto]
        CtrlConverters.controlFromDto(base)
      })
    }
  }

}
