package node

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.util.Timeout
import model.{Book, Category, User}
import repo.UserAccount
import sample.persistence.CborSerializable

import scala.concurrent.duration.DurationInt

object Node {
  val NodeServiceKey: ServiceKey[Command] = ServiceKey[Command]("node-service-key")

  trait Command extends CborSerializable

  case class AddItem(userAccount: User, replyTo: ActorRef[Command]) extends Command

  case class Token(token: String) extends Command

  case class GetToken(email: String, password: String, replyTo: ActorRef[Command]) extends Command

  case class Check(userToken: String, replyTo: ActorRef[Command]) extends Command

  case class Checked(status: String, userToken: String) extends Command

  case class Error(status: String) extends Command
//
  case class Success(result: UserAccount.SummaryUsers) extends Command

  case class SuccessUser(result: UserAccount.SummaryUser) extends Command

  case class SuccessBooks(result: UserAccount.SummaryBooks) extends Command

  case class SuccessBook(result: UserAccount.SummaryBook) extends Command

  case class SuccessCategory(result: UserAccount.SummaryCategory) extends Command

  case class SuccessCategories(result: UserAccount.SummaryCategories) extends Command

  //BOOK
  case class DeleteBook(id: String, replyTo: ActorRef[Command]) extends Command

  case class GetBook(id: String, replyTo: ActorRef[Command]) extends Command

  case class FindBook(bookName: String, replyTo: ActorRef[Command]) extends Command

  case class GetBooks(replyTo: ActorRef[Command]) extends Command

  case class CreateBook(token: String, createBook: Book, replyTo: ActorRef[Command]) extends Command

  case class UpdateBook(token: String, newBook: Book, replyTo: ActorRef[Command]) extends Command

  //CAT
  case class DeleteCategory(id: String, replyTo: ActorRef[Command]) extends Command

  case class GetCategory(id: String, replyTo: ActorRef[Command]) extends Command

  case class GetCategories(replyTo: ActorRef[Command]) extends Command

  case class CreateCategory(token: String, createCat: Category, replyTo: ActorRef[Command]) extends Command

  case class UpdateCategory(token: String, newCat: Category, replyTo: ActorRef[Command]) extends Command


  //USER ACC
  case class GetAccount(token: String, id: String, replyTo: ActorRef[Command]) extends Command

  case class GetAccounts(token: String, replyTo: ActorRef[Command]) extends Command

  case class DeleteAccount(token: String, id: String, replyTo: ActorRef[Command]) extends Command

  case class UpdateAccount(token: String, id: String, replyTo: ActorRef[Command])

  case class AddBookToAccount(bookId: String, userId:String, token: String, replyTo: ActorRef[Command]) extends Command
//
  case class Create(userAccount: User, replyTo: ActorRef[Command]) extends Command

  case class Created(userAccount: User) extends Command

  def apply(): Behavior[Command] = {
    Behaviors.setup[Command] { context =>
      implicit def system: ActorSystem[Nothing] = context.system

      implicit def scheduler: Scheduler = context.system.scheduler

      implicit lazy val timeout: Timeout = Timeout(5.seconds)
      context.system.receptionist ! Receptionist.Register(NodeServiceKey, context.self)
      val account = context.spawnAnonymous(UserAccount("users_book"))

      Behaviors.receiveMessage { message => {
        message match {
          case Check(userToken, replyTo) =>
            replyTo ! Checked("Working", userToken)

//            USER ACC
          case Create(userAccount, replyTo) =>
            account ! UserAccount.AddUser(userAccount, replyTo)
          case GetAccount(token, id, replyTo) =>
            account ! UserAccount.GetUser(token, id, replyTo)
          case DeleteAccount(token, id, replyTo) =>
            account ! UserAccount.RemoveUser(token, id, replyTo)
          case GetAccounts(token, replyTo) =>
            account ! UserAccount.GetUsers(token, replyTo)
          case AddBookToAccount(bookId, userId, token, replyTo) =>
            account ! UserAccount.AddToCart(bookId, userId, token, replyTo)

//            BOOK
          case GetBook(id, replyTo) =>
            account ! UserAccount.GetBook(id, replyTo)
          case GetBooks(replyTo) =>
            account ! UserAccount.GetBooks(replyTo)
          case FindBook(bookName, replyTo) =>
            account ! UserAccount.SearchBookByName(bookName, replyTo)
          case DeleteBook(id, replyTo) =>
            account ! UserAccount.RemoveBook(id, replyTo)
          case CreateBook(token, createBook, replyTo) =>
            account ! UserAccount.AddBook(token, createBook, replyTo)
          case UpdateBook(token, newBook, replyTo) =>
            account ! UserAccount.UpdateBook(token, newBook, replyTo)
          //            CAT
          case GetCategory(id, replyTo) =>
            account ! UserAccount.GetCategory(id, replyTo)
          case GetCategories(replyTo) =>
            account ! UserAccount.GetCategories(replyTo)
          case DeleteCategory(id, replyTo) =>
            account ! UserAccount.RemoveCategory(id, replyTo)
          case CreateCategory(id, createCat, replyTo) =>
            account ! UserAccount.AddCategory(createCat, replyTo)
          case UpdateCategory(token, category, replyTo) =>
            account ! UserAccount.UpdateCategory(token, category,  replyTo)

//            TOKEN
          case GetToken(email, password, replyTo) =>
            account ! UserAccount.GetToken(email, password, replyTo)
        }
        Behaviors.same
      }
      }
    }
  }
}
