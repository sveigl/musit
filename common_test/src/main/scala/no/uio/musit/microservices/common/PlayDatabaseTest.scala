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

package no.uio.musit.microservices.common

import org.scalatest._
import play.api.test.{FakeApplication, TestServer}

class PlayDatabaseTest extends FunSuite with Matchers with BeforeAndAfterAll {

  val additionalConfiguration:Map[String, String] = Map.apply (
    ("slick.dbs.default.driver", "slick.driver.H2Driver$"),
    ("slick.dbs.default.db.driver" , "org.h2.Driver"),
    ("slick.dbs.default.db.url" , "jdbc:h2:mem:play-test"),
    ("evolutionplugin" , "enabled")
  )
  val host = "http://localhost:7070"

  def app_ = {
    FakeApplication(additionalConfiguration = additionalConfiguration)

  }

  def server_ = TestServer(application = app_, port = 7070)

  override protected def beforeAll(): Unit = {
    server_.start

  }

  override protected def afterAll(): Unit = {
    server_.stop
  }

}