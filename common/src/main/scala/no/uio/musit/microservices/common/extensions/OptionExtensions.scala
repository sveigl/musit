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

package no.uio.musit.microservices.common.extensions

import no.uio.musit.microservices.common.domain.MusitError
import no.uio.musit.microservices.common.extensions.FutureExtensions.MusitResult

/**
 * Created by jstabel on 4/28/16.
 */

object OptionExtensions {

  implicit class OptionExtensionsImp[T](val opt: Option[T]) extends AnyVal {

    ///a quick and dirty way to get the value or throw an exception, only meant to be used for testing or quick and dirty stuff!
    def getOrFail(errorMsg: String) = {
      opt match {
        case Some(v) => v
        case None => throw new Exception(errorMsg)
      }
    }

    def toMusitResult(errorIfNone: => MusitError): MusitResult[T] = opt match {
      case Some(x) => Right(x)
      case None => Left(errorIfNone)
    } //alternative, which didn't work, I don't know why! : opt.fold(Left(errorIfNone))(Right(_))

  }

}
