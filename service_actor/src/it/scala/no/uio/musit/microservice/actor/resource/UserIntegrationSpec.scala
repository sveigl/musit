package no.uio.musit.microservice.actor.resource

import no.uio.musit.microservice.actor.domain.Person
import no.uio.musit.microservices.common.PlayTestDefaults
import no.uio.musit.security.FakeSecurity
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import no.uio.musit.microservices.common.extensions.PlayExtensions.WSRequestImp
import org.scalatest.time.{Millis, Seconds, Span}

/**
 * Created by sveigl on 21.09.16.
 */
class UserIntegrationSpec extends PlaySpec with OneServerPerSuite with ScalaFutures {

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = Span(15, Seconds),
    interval = Span(50, Millis)
  )
  override lazy val port: Int = 19010

  implicit override lazy val app = new GuiceApplicationBuilder().configure(PlayTestDefaults.inMemoryDatabaseConfig()).build()

  def withFakeUser(wsr: WSRequest, username: String) = {
    wsr.withBearerToken(FakeSecurity.fakeAccessTokenPrefix + username)
  }

  "Actor and dataporten integration" must {

    "get 401 when not providing token" in {
      val future = wsUrl("/v1/dataporten/currentUser").get()
      whenReady(future) { response =>
        response.status mustBe 401
      }
    }

    "get actor with matching dataportenId" in {
      val future = withFakeUser(wsUrl("/v1/dataporten/currentUser"), "jarle").get()
      whenReady(future) { response =>
        response.status mustBe 200
        val person = Json.parse(response.body).validate[Person].get
        person.fn mustBe "Jarle Stabell"
      }
    }
  }

}
