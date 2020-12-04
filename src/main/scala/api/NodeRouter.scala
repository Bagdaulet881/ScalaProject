package api

import java.util.UUID

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import repo.{ApiError, BookNotFound, UsersDirectives}
import node.Node
import node.Node.Checked
import model._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
case class PostText(email: String, password: String)
trait Router {
  def route: Route
}
class NodeRouter( node:ActorRef[Node.Command])(implicit system: ActorSystem[_], ex:ExecutionContext)
  extends  Router
    with  Directives with UsersDirectives{
  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler: Scheduler = system.scheduler
  override def route: Route =
    concat(
      pathPrefix("live") {
        get {
          val processFuture: Future[Checked] = node.ask(ref => Node.Check("userToken",ref))(timeout,scheduler).mapTo[Checked]
          onSuccess(processFuture){response =>
            complete(response)
          }
        }
      },
      pathPrefix("signin"){
        post{
          entity(as[PostText]) { token =>
            val processFuture: Future[Node.Token] = node.ask(
              ref => Node.GetToken(token.email, token.password,ref)
            )(timeout, scheduler).mapTo[Node.Token]
            println("send credentials")
            onSuccess(processFuture) { token =>
              complete(token)
            }
          }
        }
      },
      pathPrefix("signup"){
        post{
          entity(as[CreateUser]) { createUser =>
            val processFuture: Future[Node.SuccessUser] = node.ask(
              ref => Node.Create(
                User(UUID.randomUUID().toString, createUser.email, createUser.password, Seq.empty)
                ,ref)
            )(timeout, scheduler).mapTo[Node.SuccessUser]
            println("sended")
            onSuccess(processFuture) { response =>
              complete(response)
            }
          }
        }
      },
      pathPrefix("users") {
        concat(
          path(Segment){ id: String =>
            concat(
              get{
                val processFuture: Future[Node.SuccessUser] = node.ask(
                  ref => Node.GetAccount("user"+id, id, ref))(timeout, scheduler).mapTo[Node.SuccessUser]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              },
              delete{
                val processFuture: Future[Node.SuccessUser] = node.ask(
                  ref => Node.DeleteAccount("user" + id, id, ref))(timeout, scheduler).mapTo[Node.SuccessUser]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              },
              put{
                entity(as[AddBookToUser]) { updateUser =>
                  val processFuture: Future[Node.SuccessUser] = node.ask(
                    ref => Node.AddBookToAccount(updateUser.bookId, updateUser.userId, updateUser.userToken, ref))(timeout, scheduler).mapTo[Node.SuccessUser]
                  onSuccess(processFuture) { response =>
                    complete(response)
                  }
                }
              }
            )
          },
          get{
            val processFuture: Future[Node.Success] = node.ask(
              ref => Node.GetAccounts("admin", ref))(timeout, scheduler).mapTo[Node.Success]
            onSuccess(processFuture) { response =>
              complete(response)
            }
          },
          post{
            entity(as[CreateUser]) { createUser =>
              val processFuture: Future[Node.SuccessUser] = node.ask(
                ref => Node.Create(
                  User(UUID.randomUUID().toString, createUser.email, createUser.password, Seq.empty)
                  ,ref)
              )(timeout, scheduler).mapTo[Node.SuccessUser]
              println("send")
              onSuccess(processFuture) { response =>
                complete(response)
              }
            }
          },
        )
      },





      pathPrefix("books"){
        concat(
          pathPrefix("search"){
            path(Segment){ bookName: String =>
              get{
                val processFuture: Future[Node.SuccessBook] = node.ask(
                  ref => Node.FindBook(bookName, ref))(timeout, scheduler).mapTo[Node.SuccessBook]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              }
            }
          },
          path(Segment){ id: String =>
            concat(
              get{
                val processFuture: Future[Node.SuccessBook] = node.ask(
                  ref => Node.GetBook(id, ref))(timeout, scheduler).mapTo[Node.SuccessBook]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              },
              delete{
                val processFuture: Future[Node.SuccessBook] = node.ask(
                  ref => Node.DeleteBook(id, ref))(timeout, scheduler).mapTo[Node.SuccessBook]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              },
              put{
                entity(as[UpdateBook]) { updateBook=>
                  val processFuture: Future[Node.SuccessBook] = node.ask(
                    ref => Node.UpdateBook(updateBook.token, new Book(id,updateBook.categoryId, updateBook.name, updateBook.description), ref))(timeout, scheduler).mapTo[Node.SuccessBook]
                  onSuccess(processFuture) { response =>
                    complete(response)
                  }
                }
              }
            )
          },
          concat(
            get{
              val processFuture: Future[Node.SuccessBooks] = node.ask(
                ref => Node.GetBooks( ref))(timeout, scheduler).mapTo[Node.SuccessBooks]
              onSuccess(processFuture) { response =>
                complete(response)
              }
            },
            post{
              entity(as[CreateBook]) { createBook =>
                val processFuture: Future[Node.SuccessBook] = node.ask(
                  ref => Node.CreateBook(
                    createBook.token,
                    Book(UUID.randomUUID().toString,createBook.categoryId, createBook.name, createBook.description),ref
                  )
                )(timeout, scheduler).mapTo[Node.SuccessBook]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              }
            }
          ),
        )
      },
      pathPrefix("categories"){
        concat(
          path(Segment){ id: String =>
            concat(
              get{
                val processFuture: Future[Node.SuccessCategory] = node.ask(
                  ref => Node.GetCategory(id, ref))(timeout, scheduler).mapTo[Node.SuccessCategory]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              },
              delete{
                val processFuture: Future[Node.SuccessCategory] = node.ask(
                  ref => Node.DeleteCategory(id, ref))(timeout, scheduler).mapTo[Node.SuccessCategory]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              },
              put{
                entity(as[UpdateCategory]) { updateCat =>
                  val processFuture: Future[Node.SuccessCategory] = node.ask(
                    ref => Node.UpdateCategory(updateCat.token, new Category(id,updateCat.name, updateCat.description), ref))(timeout, scheduler).mapTo[Node.SuccessCategory]
                  onSuccess(processFuture) { response =>
                    complete(response)
                  }
                }
              }
            )
          },
          concat(
            get{
              val processFuture: Future[Node.SuccessCategories] = node.ask(
                ref => Node.GetCategories( ref))(timeout, scheduler).mapTo[Node.SuccessCategories]
              onSuccess(processFuture) { response =>
                complete(response)
              }
            },
            post{
              entity(as[CreateCategory]) { createCat =>
                val processFuture: Future[Node.SuccessCategory] = node.ask(
                  ref => Node.CreateCategory(
                    createCat.token,
                    Category(UUID.randomUUID().toString, createCat.name, createCat.description),ref
                  )
                )(timeout, scheduler).mapTo[Node.SuccessCategory]
                onSuccess(processFuture) { response =>
                  complete(response)
                }
              }
            }
          ),
        )
      }
    )
}
