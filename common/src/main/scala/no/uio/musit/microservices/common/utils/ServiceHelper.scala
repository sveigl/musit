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

package no.uio.musit.microservices.common.utils

import no.uio.musit.microservices.common.domain._
import play.api.http.Status

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Misc utilities for creating services.
 * Created by jstabel on 5/31/16.
 *
 * @author jstabel <jarle.stabell@usit.uio.no>
 *
 */
object ServiceHelper {
  import no.uio.musit.microservices.common.utils.ErrorHelper._

  def daoInsert[A](daoInsertResult: Future[A]): Future[Either[MusitError, A]] = {
    daoInsertResult.map(insertedObject => Right(insertedObject)).recover {
      case e => Left(badRequest("dao error", e.toString))
    }
  }

  /** Calls a DAO service method to update an object and returns proper result structure. Assumes a separate id (instead of the using the id likely in the objectToUpdate instance). */
  def daoUpdate[A](daoUpdateByIdCall: (Long, A) => Future[Int], idToUpdate: Long, objectToUpdate: A): Future[Either[MusitError, MusitStatusMessage]] = {
    daoUpdateByIdCall(idToUpdate, objectToUpdate).map {
      case 0 => Left(notFound("Update did not update any records!"))
      case 1 => Right(MusitStatusMessage("Record was updated!"))
      case n => Left(badRequest(s"Update updated too many records! ($n)"))
    }.recover {
      case e: MusitException => Left(e.toMusitError)
    }
  }
}
