import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._

class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)(Json.obj("status" -> statusCode, "message" -> message))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful(
      InternalServerError(Json.obj("status" -> 500, "message" -> exception.getMessage))
    )
  }
}