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

package no.uio.musit.service

object MusitResults {

  sealed abstract class MusitResult[+A] {

    def isSuccess: Boolean

    def isFailure: Boolean = !isSuccess

    def get: A

    def map[B](f: A => B): MusitResult[B] = this match {
      case MusitSuccess(success) => MusitSuccess(f(success))
      case err: MusitError => err
    }

    def flatMap[B](f: A => MusitResult[B]): MusitResult[B] = this match {
      case MusitSuccess(success) => f(success)
      case err: MusitError => err
    }

    def flatten[B](implicit ev: A <:< MusitResult[B]): MusitResult[B] = {
      this match {
        case MusitSuccess(success) => success
        case err: MusitError => err
      }
    }

    final def getOrElse[B >: A](default: => B): B = {
      if (isFailure) default else this.get
    }

  }

  /**
   * Use this to as return type when operations is successful
   */
  case class MusitSuccess[+A](value: A) extends MusitResult[A] {
    override val isSuccess: Boolean = true

    override def get: A = value
  }

  sealed trait MusitError extends MusitResult[Nothing] {
    val message: String

    override def get: Nothing =
      throw new NoSuchElementException("MusitResult.get on MusitError") // scalastyle:ignore
  }

  // ========================================================================
  // Specific error types inheriting from MusitError
  // ========================================================================

  /**
   * A special result case to represent a status where there was no argument.
   * It's really a success state, but it handles better in the code if treated
   * here as a MusitError type.
   */
  case object MusitEmpty extends MusitError {
    override val message: String = "empty"
    override val isSuccess: Boolean = true
  }

  /**
   * Use this when validation of fields and conditions are not met.
   */
  case class MusitValidationError(
      message: String,
      expected: Option[Any] = None,
      actual: Option[Any] = None
  ) extends MusitError {
    override val isSuccess: Boolean = false
  }

  /**
   * Use this when dealing with unexpected internal errors.
   */
  case class MusitInternalError(message: String) extends MusitError {
    override val isSuccess: Boolean = false
  }

  /**
   * Use this error type when handling Exceptions from the DB driver.
   */
  case class MusitDbError(
      message: String,
      ex: Option[Throwable] = None
  ) extends MusitError {
    override val isSuccess: Boolean = false
  }

}
