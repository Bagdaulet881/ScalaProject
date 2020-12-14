package node

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.util.Timeout
import model.{Book, Category, Coffee, User}
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
//
  case class SuccessCoffees(result: UserAccount.SummaryCoffees) extends Command

  case class SuccessCoffee(result: UserAccount.SummaryCoffee) extends Command
//
  case class SuccessCategory(result: UserAccount.SummaryCategory) extends Command

  case class SuccessCategories(result: UserAccount.SummaryCategories) extends Command
//
  case class SuccessOrder(result: UserAccount.SummaryOrder) extends Command

  case class SuccessOrders(result: UserAccount.SummaryOrders) extends Command

  //ORDER
  case class GetOrders(replyTo: ActorRef[Command]) extends Command

  //COFFEE
  case class DeleteCoffee(id: String, replyTo: ActorRef[Command]) extends Command

  case class GetCoffee(id: String, replyTo: ActorRef[Command]) extends Command

//  case class FindBook(bookName: String, replyTo: ActorRef[Command]) extends Command

  case class GetCoffees(replyTo: ActorRef[Command]) extends Command

  case class CreateCoffee(token: String, createCoffee: Coffee, replyTo: ActorRef[Command]) extends Command

  case class UpdateCoffee(token: String, updatedCoffee: Coffee, replyTo: ActorRef[Command]) extends Command

  case class RateCoffee(userToken: String, coffeeId: String, newRate: Int, replyTo: ActorRef[Command]) extends Command


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

  case class OrderCoffee(coffeeId: String, userId:String, token: String, replyTo: ActorRef[Command]) extends Command
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
          case OrderCoffee(coffeeId, userId, token, replyTo) =>
            account ! UserAccount.AddToOrderList(coffeeId, userId, token, replyTo)

//        ORDER
          case GetOrders(replyTo) =>
            account ! UserAccount.GetOrders(replyTo)
//        COFFEE
          case GetCoffee(id, replyTo) =>
            account ! UserAccount.GetCoffee(id, replyTo)
          case GetCoffees(replyTo) =>
            account ! UserAccount.GetCoffees(replyTo)
//          case FindBook(bookName, replyTo) =>
//            account ! UserAccount.SearchBookByName(bookName, replyTo)
          case DeleteCoffee(id, replyTo) =>
            account ! UserAccount.RemoveCoffee(id, replyTo)
          case CreateCoffee(token, createCoffee, replyTo) =>
            account ! UserAccount.AddCoffee(token, createCoffee, replyTo)
          case UpdateCoffee(token, newCoffee, replyTo) =>
            account ! UserAccount.UpdateCoffee(token, newCoffee, replyTo)
          case RateCoffee(userToken, coffeeId, rating, replyTo) =>
            account ! UserAccount.RateCoffee(userToken, coffeeId, rating, replyTo)


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
