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
import no.uio.musit.microservice.actor.domain.Organization
import no.uio.musit.microservice.actor.service.OrganizationService
import no.uio.musit.microservices.common.domain.{MusitError, MusitSearch, MusitStatusMessage}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

class OrganizationResource @Inject() (orgService: OrganizationService) extends Controller {

  def listRoot(museumId: Int, search: Option[MusitSearch]): Action[AnyContent] = Action.async { request =>
    search match {
      case Some(criteria) => orgService.find(criteria).map(orgs => Ok(Json.toJson(orgs)))
      case None => orgService.all.map(org => Ok(Json.toJson(org)))
    }
  }

  def getRoot(id: Long): Action[AnyContent] = Action.async { request =>
    orgService.find(id).map {
      case Some(person) => Ok(Json.toJson(person))
      case None => NotFound(Json.toJson(MusitError(NOT_FOUND, s"Did not find object with id: $id")))
    }
  }

  def postRoot: Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
    val actorResult: JsResult[Organization] = request.body.validate[Organization]
    actorResult match {
      case s: JsSuccess[Organization] => orgService.create(s.get).map(org => Created(Json.toJson(org)))
      case e: JsError => Future.successful(BadRequest(Json.toJson(MusitError(BAD_REQUEST, e.toString))))
    }
  }

  def updateRoot(id: Long): Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
    val actorResult: JsResult[Organization] = request.body.validate[Organization]
    actorResult match {
      case s: JsSuccess[Organization] =>
        orgService.update(s.get).map {
          case Right(updateStatus) => Ok(Json.toJson(updateStatus))
          case Left(error) => Status(error.status)(Json.toJson(error))
        }
      case e: JsError => Future.successful(BadRequest(Json.toJson(MusitError(BAD_REQUEST, e.toString))))
    }
  }

  def deleteRoot(id: Long): Action[AnyContent] = Action.async { request =>
    orgService.remove(id).map { noDeleted =>
      Ok(Json.toJson(MusitStatusMessage(s"Deleted $noDeleted record(s).")))
    }
  }
}
