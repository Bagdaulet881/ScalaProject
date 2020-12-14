package repo
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.{Directive1, Directives}
import scala.concurrent.Future
import scala.util.Success

final case class UserNotFound(id: String) extends Exception("User " + id + " not found")
final case class BookNotFound(id: String) extends Exception("Book " + id + " not found")
final case class BookFound(id: String) extends Exception("Book " + id + "  is already in list")



final case class ApiError private(statusCode: StatusCode, message: String)
object ApiError {
  private def apply(statusCode: StatusCode, message: String): ApiError = new ApiError(statusCode, message)

  val generic: ApiError = new ApiError(StatusCodes.InternalServerError, "Unknown error.")
  val emptyTitleField: ApiError = new ApiError(StatusCodes.BadRequest, "The title field must not be empty.")
  val emptyNameField: ApiError = new ApiError(StatusCodes.BadRequest, "The Name field must not be empty.")
  val emptyEmailField: ApiError = new ApiError(StatusCodes.BadRequest, "The Email field must not be empty.")
  val emptyPhoneField: ApiError = new ApiError(StatusCodes.BadRequest, "The Phone field must not be empty.")
  val emptyDescriptionField: ApiError = new ApiError(StatusCodes.BadRequest, "The description field must not be empty.")
  val emailWrongFormat: ApiError = new ApiError(StatusCodes.BadRequest, "The email field is wrong.")
  val phoneWrongFormat: ApiError = new ApiError(StatusCodes.BadRequest, "The phone number field is wrong.")

  def todoNotFound(id: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The todo with id $id could not be found.")

  def addressNotFound(id: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The addressBook with id $id could not be found.")
}

trait UsersDirectives extends Directives {
  def handle[T](f: Future[T])(e: Throwable => ApiError): Directive1[T] = onComplete(f) flatMap {
    case Success(t) =>
      provide(t)
  }
}