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

/**
 * Created by jstabel on 4/12/16.
 */
object SeqExtensions {

  implicit class SeqExtensionsImp[T](val seq: Seq[T]) extends AnyVal {
    def hasAllOf(items: Seq[T]) = (items == null) || items.forall(g => seq.contains(g))

    def hasNoneOf(items: Seq[T]) = (items == null) || items.forall(g => !seq.contains(g))

  }

}
