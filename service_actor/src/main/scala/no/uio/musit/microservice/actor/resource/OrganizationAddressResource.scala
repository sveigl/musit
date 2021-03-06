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
package no.uio.musit.microservice.actor.resource

import com.google.inject.Inject
import no.uio.musit.microservice.actor.domain.OrganizationAddress
import no.uio.musit.microservice.actor.service.OrganizationAddressService
import no.uio.musit.microservices.common.domain.{MusitError, MusitStatusMessage}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

class OrganizationAddressResource @Inject() (
    val orgAdrService: OrganizationAddressService
) extends Controller {

  def listRoot(organizationId: Long): Action[AnyContent] = Action.async { request =>
    orgAdrService.all(organizationId).map(addr => {
      Ok(Json.toJson(addr))
    })
  }

  def getRoot(organizationId: Long, id: Long): Action[AnyContent] = Action.async { request =>
    orgAdrService.find(id).map {
      case Some(addr) => Ok(Json.toJson(addr))
      case None => NotFound(Json.toJson(MusitError(NOT_FOUND, s"Did not find object with id: $id")))
    }
  }

  def postRoot(organizationId: Long): Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
    val actorResult: JsResult[OrganizationAddress] = request.body.validate[OrganizationAddress]
    actorResult match {
      case s: JsSuccess[OrganizationAddress] =>
        orgAdrService.create(s.get).map { newAddr => Created(Json.toJson(newAddr)) }
      case e: JsError => Future.successful(BadRequest(Json.toJson(MusitError(BAD_REQUEST, e.toString))))
    }
  }

  def updateRoot(organizationId: Long, id: Long): Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
    val actorResult: JsResult[OrganizationAddress] = request.body.validate[OrganizationAddress]
    actorResult match {
      case s: JsSuccess[OrganizationAddress] =>
        orgAdrService.update(s.get).map {
          case Right(statusMessage) => Ok(Json.toJson(statusMessage))
          case Left(error) => Status(error.status)(Json.toJson(error))
        }
      case e: JsError => Future.successful(BadRequest(Json.toJson(MusitError(BAD_REQUEST, e.toString))))
    }
  }

  def deleteRoot(organizationId: Long, id: Long): Action[AnyContent] = Action.async { request =>
    orgAdrService.remove(id).map { noDeleted =>
      Ok(Json.toJson(MusitStatusMessage(s"Deleted $noDeleted record(s).")))
    }
  }
}
