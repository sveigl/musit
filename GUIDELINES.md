MUSIT Code Guidelines
=====================

# Scala

## Style
The MUSIT code style follows the Scala Style Guide. The only exception is the style of block comments:

```scala
/**
  * Style mandated by "Scala Style Guide"
  */
 
/**
 * Style adopted in the MUSIT codebase
 */
```
 
MUSIT is using Scalariform to format the source code as part of the build. So just hack away and then run sbt compile and it will reformat the code according to MUSIT standards.

## Playframework
The microservices is produced using playframework and docker. The project is diverging from the normal Playframework namespaces to make sure the developer is aware he is not making traditional webpages or monolith apis.

All route definitions have to comply to REST best practices for RESTv2 and HATEOS. 

The microservice is namespaced like business logic and the base namespace is:

```scala
package no.uio.musit.microservice
```

The subprojects name starts with: service_

So for an **actor** service the project name would be service_actor in sbt and the namespace would start with:

```scala
package no.uio.musit.microservice.actor
```

Inside the microservice we have some predefined subnamespaces we use:

| Subnamespace      | Description |
|-------------------|-------------|
| domain            | All case classes and formatters are placed in this namespace. |
| service           | All scala api traits containing businesslogic is placed in this namespace. |
| resource          | The REST protocol wrapper, here you place the web controller to expose the service api to HTTP. |
| dao               | All data access objects, slick implementation and database wrappers are defined here. |

Example: making an actor controller, namespace would be:

```scala
package no.uio.musit.microservice.actor.resource
```

Example route definition for an actor service:

```scala
# Routes
# This file defines all application routes (Higher priority routes first)
# ~~~~

# Setup endpoints

## Mapping routes, TODO: Remove these enpoints when database is migrated.
GET     /v1/person                                 no.uio.musit.microservice.actor.resource.LegacyPersonResource.list(search: Option[no.uio.musit.microservices.common.domain.MusitSearch])
GET     /v1/person/:id                             no.uio.musit.microservice.actor.resource.LegacyPersonResource.getById(id:Long)

## Organization routes, the new actor
GET     /v1/organization                           no.uio.musit.microservice.actor.resource.OrganizationResource.listRoot(search: Option[no.uio.musit.microservices.common.domain.MusitSearch])
POST    /v1/organization                           no.uio.musit.microservice.actor.resource.OrganizationResource.postRoot
PUT     /v1/organization/:orgId                    no.uio.musit.microservice.actor.resource.OrganizationResource.updateRoot(orgId:Long)
GET     /v1/organization/:orgId                    no.uio.musit.microservice.actor.resource.OrganizationResource.getRoot(orgId:Long)
DELETE  /v1/organization/:orgId                    no.uio.musit.microservice.actor.resource.OrganizationResource.deleteRoot(orgId:Long)

## OrganizationAddress routes, the new actor
GET     /v1/organization/:orgId/address            no.uio.musit.microservice.actor.resource.OrganizationAddressResource.listRoot(orgId:Long)
POST    /v1/organization/:orgId/address            no.uio.musit.microservice.actor.resource.OrganizationAddressResource.postRoot(orgId:Long)
PUT     /v1/organization/:orgId/address/:id        no.uio.musit.microservice.actor.resource.OrganizationAddressResource.updateRoot(orgId:Long, id:Long)
GET     /v1/organization/:orgId/address/:id        no.uio.musit.microservice.actor.resource.OrganizationAddressResource.getRoot(orgId:Long, id:Long)
DELETE  /v1/organization/:orgId/address/:id        no.uio.musit.microservice.actor.resource.OrganizationAddressResource.deleteRoot(orgId:Long, id:Long)
```

## Option and what to use where
When writing logic in scala we frown upon using Exceptions of any sorts. Exceptions are exceptions and code we control do not throw exceptions.

Scala has many good tools to enable the developers write pure functions, and some of these namely:

* Option
* Either
* Try

We use for different things.

### Option
We primarily use plain Option for transfering results and attributes we do not know if will exist or not, we frown upon using null as a value in our functions and logic.

### Try
An excelent newcomer into the Scala language from twitter which excels at wrapping external libraries we have no controll over. We can wrap logic in a try to easilly handle methods that throw exceptions much the same way as futures handles exceptions.

Try should NOT be used to indicate fail or success in code we control where we can avoid exceptions.

Example use when wrapping java libraries that is exception heavy:

```scala
import scala.util.Try
import java.net.URL
def parseURL(url: String): Try[URL] = Try(new URL(url))

val url = parseURL(Console.readLine("URL: ")) getOrElse new URL("http://example.foo")
```
Note that try will only work for non-fatal exceptions.

### Either
This is MUSITs bread and butter for return values from business methods where we validate and fail logic. Either is strongly typed and been used for this purpose for a long time by Scala developers.

MUSIT define a right handed approach to Either, much the same way as Playframework does. This means the error should be Left while the success should be Right in the either.

Example of use in a service:

```scala
def update(person: Person): Future[Either[MusitError, MusitStatusMessage]] = {
  ActorDao.updatePerson(person).map {
    case 0 => Left(MusitError(Status.BAD_REQUEST, "Update did not update any records!"))
    case 1 => Right(MusitStatusMessage("Record was updated!"))
    case _ => Left(MusitError(Status.BAD_REQUEST, "Update updated several records!"))
  }
}
```
Note that the error is a MusitError or a subtype of MusitError, it should NOT be an exception.

## Futures
All endpoints in the resources for the microservices have to be of type **Action.async**, this require the use of Futures when returning the protocol wrapper code from the endpoint.

Do not use a Future just for using a Future. You keep the code clean and simple, futures are introduced where you really need futures. When you come to the point where you need to convert the code to a future, you do that explisitly where its needed, see an example from the time service which do not require the use of Futures to process time:

```scala
package no.uio.musit.microservices.time.resource

import no.uio.musit.microservices.common.domain.{ MusitFilter, MusitSearch }
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.Future
import no.uio.musit.microservices.time.service.TimeService

class TimeResource extends Controller with TimeService {

  def now(filter: Option[MusitFilter], search: Option[MusitSearch]) = Action.async { request =>
    Future.successful(
      convertToNow(filter) match {
        case Right(mt) => Ok(Json.toJson(mt))
        case Left(err) => Status(err.status)(Json.toJson(err))
      }
    )
  }

}
```
Notice how were using Future.successful() inside the endpoint itself, it should never be implemented in the service class if its not needed.

Example of using futures where api we consume is async like Slick:

```scala
// Dao method (data access logic)
def updateOrganization(organization: Organization): Future[Int] = {
  db.run(OrganizationTable.filter(_.id === organization.id).update(organization))
}

// Service method (business logic and validation logic)
def update(organization: Organization): Future[Either[MusitError, MusitStatusMessage]] = {
  ActorDao.updateOrganization(organization).map {
    case 0 => Left(MusitError(Status.BAD_REQUEST, "Update did not update any records!"))
    case 1 => Right(MusitStatusMessage("Record was updated!"))
    case _ => Left(MusitError(Status.BAD_REQUEST, "Update updated several records!"))
  }
}

// Endpoint (only REST spesific coding)
def updateRoot(id: Long): Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
  val actorResult: JsResult[Organization] = request.body.validate[Organization]
  actorResult match {
    case s: JsSuccess[Organization] =>
      update(s.get).map {
        case Right(updateStatus) => Ok(Json.toJson(updateStatus))
        case Left(error) => Status(error.status)(Json.toJson(error))
      }
    case e: JsError => Future.successful(BadRequest(Json.toJson(MusitError(BAD_REQUEST, e.toString))))
  }
}
```

Notice we never introduce a Future where its not needed for extra paralellism, we work as sequencial as we can until we really need to pull a future or the framework require us to use one.

Notice when we resolve a Future[Option] we use the block signature of map (map {}), then we can case directly on the option values of the construct instead of mapping the Future first the match on the Option, its a convenient shortcut that makes the code much easier to read and use.

## Testing
We use scalatest as testframework, to be exact were using the extension to play named scalatestplus.
We have two different test types enabled in the project, unit tests and integration tests.

You find the different test types in the following directories in the microservice subprojects:

* {service_any}/test
* {service_any}/src/it

Any test that access logic outside the method you are testing, like an external endpoint, database or similar is to be placed in the integration test suite (it). All self contained tests that only test code inside a method not reaching outside its own logic is to be made as a unit test (test).

Spesifically all tests using traits (or similar traits):

* OneServerPerSuite
* OneAppPerSuite

Is an integration test.

Example of integration test:

```scala
package no.uio.musit.microservice.actor.resource

import no.uio.musit.microservice.actor.dao.ActorDao
import no.uio.musit.microservice.actor.domain.Person
import no.uio.musit.microservices.common.PlayTestDefaults
import no.uio.musit.microservices.common.linking.LinkService
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.inject.guice.GuiceApplicationBuilder

class LegacyPersonUnitTest extends PlaySpec with OneAppPerSuite with ScalaFutures {

  val timeout = PlayTestDefaults.timeout
  implicit override lazy val app = new GuiceApplicationBuilder().configure(PlayTestDefaults.inMemoryDatabaseConfig()).build()

  "Actor dao" must {
    import ActorDao._


    "getById_kjempeTall" in {
      val svar = getPersonLegacyById(6386363673636335366L)
      whenReady(svar, timeout) { thing =>
        assert (thing == None)
      }
    }

    "getById__Riktig" in {
      val svar = getPersonLegacyById(1)
      whenReady(svar, timeout) { thing =>
        assert (thing == Some(Person(1, "And, Arne1", links = Seq(LinkService.self("/v1/person/1")))))
      }
    }

    "getById__TalletNull" in {
      val svar = getPersonLegacyById(0)
      whenReady(svar, timeout) { thing =>
        assert (thing == None)
      }
    }
  }
}
```

Example of unit test:

```scala
package controllers

// common imports
import play.api.test.FakeRequest
import play.api.libs.json.Json
import org.scalatest.ParallelTestExecution
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._

// test imports
import no.uio.musit.microservices.common.domain.{ MusitError, MusitFilter }
import no.uio.musit.microservices.time.resource.TimeResource
import no.uio.musit.microservices.time.domain.MusitTime

class TimeControllerSpec extends PlaySpec with ScalaFutures with ParallelTestExecution {
  "TimeController" must {
    "give date and time when provided a datetime filter" in {
      val futureResult = new TimeResource().now(Some(MusitFilter(List("date", "time"))), None).apply(FakeRequest())
      status(futureResult) mustBe OK
      val json = contentAsString(futureResult)
      val now = Json.parse(json).validate[MusitTime].get
      now.time must not be None
      now.date must not be None
    }

    "give date but not time when provided a date filter" in {
      val futureResult = new TimeResource().now(Some(MusitFilter(List("date"))), None)(FakeRequest())
      status(futureResult) mustBe OK
      val json = contentAsString(futureResult)
      val now = Json.parse(json).validate[MusitTime].get
      now.time mustBe None
      now.date must not be None
    }

    "give time but not date when provided a time filter" in {
      val futureResult = new TimeResource().now(Some(MusitFilter(List("time"))), None)(FakeRequest())
      status(futureResult) mustBe OK
      val json = contentAsString(futureResult)
      val now = Json.parse(json).validate[MusitTime].get
      now.date mustBe None
      now.time must not be None
    }

    "give date and time when provided no filter" in {
      val futureResult = new TimeResource().now(None, None)(FakeRequest())
      status(futureResult) mustBe OK
      val json = contentAsString(futureResult)
      val now = Json.parse(json).validate[MusitTime].get
      now.date must not be None
      now.time must not be None
    }

    "give error message when provided invalid filter" in {
      val futureResult = new TimeResource().now(Some(MusitFilter(List("uglepose"))), None)(FakeRequest())
      status(futureResult) mustBe BAD_REQUEST
      val json = contentAsString(futureResult)
      val now = Json.parse(json).validate[MusitError].get
      now.message mustBe "Only supports empty filter or filter on time, date or time and date"
    }
  }
}
```

# JavaScript

TBD

# GIT

TBD

# Development process

To work with the code we have defined a process we need to follow to ensure quality and tracking control for the code base.

General flow:

1. Get a jira issue for the work task.
2. Create a branch for the jira issue.
3. Commit your work often, and push and pull often (minimum when you arrive in the morning, before lunch and when you leave for the day)
4. ```sbt compile test it:test``` must run flawlessly from local machine when you are done, without style problems.
5. in gui folder, ```npm run lint:fix``` should pass without errors or warnings
6. Push the last version of the code changes. (make sure you have staged style changes from running tests and linting)
7. Create a pull request for the branch to master.
8. Pull request review needs to be done by a 2nd pair of eyes, and control checks in github has to pass.
9. Merge with squashing commits when reviewed and code is ok.
10. Delete work branch.

