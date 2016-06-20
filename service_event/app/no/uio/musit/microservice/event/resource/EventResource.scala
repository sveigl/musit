/*
 *   MUSIT is a cooperation between the university museums of Norway.
 *   Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License,
 *   or any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along
 *   with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package no.uio.musit.microservice.event.resource

import io.swagger.annotations.ApiOperation
import no.uio.musit.microservice.event.domain.{ AtomLink, CompleteEvent, EventInfo, EventType }
import no.uio.musit.microservice.event.service.EventService
import no.uio.musit.microservices.common.domain.MusitError
import no.uio.musit.microservices.common.utils.Misc._
import no.uio.musit.microservices.common.utils.{ ErrorHelper, ResourceHelper }
import play.api.libs.json._
import play.api.mvc.{ Action, BodyParsers, Controller }

import scala.util.{ Failure, Success, Try }

/**
 * Created by jstabel on 6/10/16.
 */

class EventResource extends Controller {

  def eventInfoToJson(eventInfo: EventInfo) = Json.toJson(eventInfo)

  def jsonToEventInfo(json: JsValue): Either[MusitError, EventInfo] = {
    // TODO: Better error handling
    val eventTypeName = (json \ "eventType").as[String]
    val optJsObject = (json \ "eventData").toOption.map(_.as[JsObject])
    val links = ((json \ "links").asOpt[List[AtomLink]]).getOrElse(Seq.empty)
    val maybeEventType = Try(EventType(eventTypeName))
    maybeEventType match {
      case Success(eventType) => Right(EventInfo(None, eventTypeName, optJsObject, Some(links)))
      case Failure(e) => Left(ErrorHelper.badRequest(e.getMessage))
    }

  }

  @ApiOperation(value = "Event operation - inserts an Event", httpMethod = "POST")
  def postRoot: Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>

    val eventInfoResult = jsonToEventInfo(request.body)
    ResourceHelper.postRootWithMusitResult(EventService.createEvent, eventInfoResult, eventInfoToJson)
  }

  def getRoot(id: Long) = Action.async { request =>
    def completeEventToEventInfoToJson(completeEvent: CompleteEvent) = {
      EventService.completeEventToEventInfo(completeEvent) |> eventInfoToJson
    }
    ResourceHelper.getRoot(EventService.getById, id, completeEventToEventInfoToJson)
  }
}