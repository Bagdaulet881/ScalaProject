package repo

import java.time.Instant
import java.util.UUID

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import model.{Book, Category, Coffee, Order, User}
import sample.persistence.CborSerializable
import node.Node
import node.Node.Command

import scala.concurrent.duration.DurationInt
object UserAccount {

  final case class State(users: Map[String, User], books: Map[String, Book], cats: Map[String, Category],coffees: Map[String, Coffee], orders: Map[String, Order], checkoutDate: Option[Instant]) extends CborSerializable {
    def isCheckedOut: Boolean =
      checkoutDate.isDefined

    def hasUser(itemId: String): Boolean =
      users.contains(itemId)

    def isEmptyUser: Boolean =
      users.isEmpty
    def getUserById(id: String): User ={
      users(id)
    }
    def addToCart(coffeeId:String, user: User): User ={
      val newOrder = new Order(UUID.randomUUID().toString, user.id, coffeeId)
      updateOrder(newOrder)
      var newOrderList = user.orders :+ getCoffeeById(coffeeId)
      var changedUser = user.copy(orders=newOrderList)
      changedUser
    }
    def updateUser(itemId: String, user: User): State = {
      hasUser(itemId) match {
        case true =>
          copy(users = users - itemId)
          copy(users = users + (itemId -> user))
        case false =>
          copy(users = users + (user.id -> user))
      }
    }

    def removeUser(itemId: String): State =
      copy(users = users - itemId)

//ORDER
    def hasOrder(itemId: String): Boolean =
      orders.contains(itemId)
    def updateOrder(order: Order): State = {
      hasOrder(order.id) match {
        case true =>
          copy(orders = orders - order.id)
          copy(orders = orders + (order.id -> order))
        case false =>
          copy(orders = orders + (order.id -> order))
      }
    }
    def toSummaryOrders: SummaryOrders = {
      val userList = users.values.toList
      var orderList = Array[Order]()
      for(user <- userList){
        for(order <- user.orders){
          orderList = orderList :+ new Order(UUID.randomUUID().toString, user.id, order.id)
        }
      }
      SummaryOrders(orderList, isCheckedOut)
    }

    def toSummaryOrder(id: String): SummaryOrder =
      SummaryOrder(orders.get(id), isCheckedOut)
//COFFEE
    def hasCoffee(itemId: String): Boolean =
      coffees.contains(itemId)

    def isEmptyCoffees: Boolean =
      coffees.isEmpty
    def getCoffeeById(coffeeId: String): Coffee = {
      coffees(coffeeId)
    }
    def updateCoffee(itemId: String, coffee: Coffee): State = {
      hasCoffee(itemId) match {
        case true =>
          copy(coffees = coffees - itemId)
          copy(coffees = coffees + (itemId -> coffee))
        case false =>
          copy(coffees = coffees + (coffee.id -> coffee))

      }
    }
    def rateCoffee(coffeeId: String, newRate: Int): State = {
        val temp = getCoffeeById(coffeeId)
        val newList = temp.rating :+ newRate
        val newCoffee = temp.copy(rating = newList)
      updateCoffee(newCoffee.id, newCoffee)
    }
    def removeCoffee(itemId: String): State =
      copy(coffees = coffees - itemId)
    def toSummaryCoffees: SummaryCoffees =
      SummaryCoffees(coffees, isCheckedOut)
    def toSummaryCoffee(id: String): SummaryCoffee =
      SummaryCoffee(coffees.get(id), isCheckedOut)
//CAT
  def hasCategory(itemId: String): Boolean =
    cats.contains(itemId)
  def toSummaryCat(id: String): SummaryCategory =
    SummaryCategory(cats.get(id), isCheckedOut)
  def toSummaryCats: SummaryCategories =
    SummaryCategories(cats, isCheckedOut)

  def updateCategory(itemId: String, cat: Category): State = {
    hasCategory(itemId) match {
      case true =>
        copy(cats = cats - itemId)
        copy(cats = cats + (itemId -> cat))
      case false =>
        copy(cats = cats + (cat.id -> cat))

    }
  }
  def removeCategory(itemId: String): State =
    copy(cats = cats - itemId)

//
    def checkout(now: Instant): State =
      copy(checkoutDate = Some(now))

    def toSummaryUsers: SummaryUsers =
      SummaryUsers(users, isCheckedOut)

    def toSummaryUser(id: String): SummaryUser =
      SummaryUser(users.get(id), isCheckedOut)


//BOOK



  }

  object State {
    val empty: State = State(users = Map.empty, books = Map.empty, cats = Map.empty, coffees = Map.empty, orders = Map.empty, checkoutDate = None)
  }
//USER ACC
  final case class AddUser(userAccount: User, replyTo: ActorRef[Node.Command]) extends Command

  final case class RemoveUser(token: String, itemId: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class AdjustUser(itemId: String, userAccount: User, replyTo: ActorRef[Node.Command]) extends Command

  final case class AddToOrderList(bookId: String, userId:String, userToken: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetUser(token: String, id: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetUsers(token: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class SummaryUsers(users: Map[String, User], checkedOut: Boolean) extends Command

  final case class SummaryUser(user: Option[User], checkedOut: Boolean) extends Command
  //ORDER
  final case class GetOrder(id: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetOrders(replyTo: ActorRef[Node.Command]) extends Command

  //COFFEE
  final case class AddCoffee(token:String, coffee: Coffee, replyTo: ActorRef[Node.Command]) extends Command

  final case class RemoveCoffee(itemId: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class UpdateCoffee(token: String, newCoffee: Coffee, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetCoffee(id: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetCoffees(replyTo: ActorRef[Node.Command]) extends Command

  final case class RateCoffee(userToken: String, coffeeId: String, newRate: Int, replyTo: ActorRef[Node.Command]) extends Command


//CAT
  final case class AddCategory(cat: Category, replyTo: ActorRef[Node.Command]) extends Command

  final case class RemoveCategory(itemId: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetCategory(id: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class GetCategories(replyTo: ActorRef[Node.Command]) extends Command

  final case class UpdateCategory(token: String, newCategory: Category, replyTo: ActorRef[Node.Command]) extends Command





//
  final case class SummaryOrders(orders: Array[Order], checkedOut: Boolean) extends Command

  final case class SummaryOrder(order: Option[Order], checkedOut: Boolean) extends Command

  final case class SummaryCoffees(coffees: Map[String, Coffee], checkedOut: Boolean) extends Command

  final case class SummaryCoffee(coffee: Option[Coffee], checkedOut: Boolean) extends Command

  final case class SummaryCategory(category: Option[Category], checkedOut: Boolean) extends Command

  final case class SummaryCategories(cats: Map[String, Category], checkedOut: Boolean) extends Command

//
  final case class GetToken(email: String, password: String, replyTo: ActorRef[Node.Command]) extends Command

  final case class Checkout(replyTo: ActorRef[Node.Command]) extends Command

  final case class CheckoutBook(replyTo: ActorRef[Node.Command]) extends Command

  sealed trait Event extends CborSerializable {
    def cartId: String
  }
//
  final case class UserAdded(cartId: String, itemId: String, userAccount: User) extends Event

  final case class UserRemoved(cartId: String, itemId: String) extends Event

  final case class UserAdjusted(cartId: String, itemId: String, userAccount: User) extends Event

  final case class UserCartUpdated(cartId: String, bookId: String, userId: String) extends Event

  final case class OrderAdded(cartId: String, itemId: String, order: Order) extends Event

  //
  final case class CoffeeAdded(cartId: String, itemId: String, coffee: Coffee) extends Event

  final case class CoffeeRemoved(cartId: String, itemId: String) extends Event

  final case class CoffeeAdjusted(cartId: String, itemId: String, coffee: Coffee) extends Event

  final case class CoffeeRated(cartId: String, itemId: String, newRate: Int) extends Event
//
  final case class CategoryAdded(cartId: String, itemId: String, cat: Category) extends Event

  final case class CategoryRemoved(cartId: String, itemId: String) extends Event

  final case class CategoryAdjusted(cartId: String, itemId: String, cat: Category) extends Event
//
  final case class CheckedOut(cartId: String, eventTime: Instant) extends Event

  def apply(cartId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("ShoppingCart", cartId),
      State.empty,
      (state, command) =>
        if (state.isCheckedOut) checkedOutShoppingCart(cartId, state, command)
        else openShoppingCart(cartId, state, command),
      (state, event) => handleEvent(state, event))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }

  private def openShoppingCart(cartId: String, state: State, command: Command): Effect[Event, State] =
    command match {
      case GetUsers(token, replyTo) =>
        if (token == "admin")
          replyTo ! Node.Success(state.toSummaryUsers)
        Effect.none
      case AddUser(userAccount, replyTo) =>
        if (state.hasUser(userAccount.id)) {
          replyTo ! Node.Error(s"Item '${userAccount.id}' was already added")
          Effect.none
        } else if (userAccount.email == "" || userAccount.password == "") {
          replyTo ! Node.Error("Name or Pass mustn't be empty")
          Effect.none
        } else {
          Effect
            .persist(UserAdded(cartId, userAccount.id, userAccount))
            .thenRun(updatedCart => replyTo ! Node.SuccessUser(updatedCart.toSummaryUser(userAccount.id)))
        }
      case AddToOrderList(coffeeId, userId, userToken, replyTo) =>

        if (state.hasUser(userId)) {
          if("user" + userId == userToken || state.hasCoffee(coffeeId)){
            Effect
              .persist(UserCartUpdated(cartId, coffeeId, userId))
              .thenRun(updatedCart => replyTo ! Node.SuccessUser(updatedCart.toSummaryUser(userId)))
          }else{
            replyTo ! Node.Error(s"Cant add book to cart, token expired or book not found")
            Effect.none
          }
        } else {
          replyTo ! Node.Error(s"Cant find user with id: '${userId}'")
          Effect.none

        }
      case RemoveUser(token, itemId, replyTo) =>
        if (state.hasUser(itemId) && token == "user" + itemId) {
          Effect.persist(UserRemoved(cartId, itemId)).thenRun(updatedCart => replyTo ! Node.SuccessUser(updatedCart.toSummaryUser(itemId)))
        } else {
          replyTo ! Node.Success(state.toSummaryUsers) // removing an item is idempotent
          Effect.none
        }
      case AdjustUser(itemId, userAccount, replyTo) =>
        if (userAccount.email == "" || userAccount.password == "") {
          replyTo ! Node.Error("Name or Pass mustn't be empty")
          Effect.none
        } else if (state.hasUser(itemId)) {
          Effect
            .persist(UserAdjusted(cartId, itemId, userAccount))
            .thenRun(updatedCart => replyTo ! Node.Success(updatedCart.toSummaryUsers))
        } else {
          replyTo ! Node.Error(s"Cannot adjust quantity for item '$itemId'. Item not present on cart")
          Effect.none
        }
//        --------------------------------------------------------------------------------------------------------------
      case Checkout(replyTo) =>
        if (state.isEmptyUser) {
          replyTo ! Node.Error("Cannot checkout an empty shopping cart")
          Effect.none
        } else {
          Effect
            .persist(CheckedOut(cartId, Instant.now()))
            .thenRun(updatedCart => replyTo ! Node.Success(updatedCart.toSummaryUsers))
        }
      case GetUser(token, id, replyTo) =>
        if (token == "user" + id && state.hasUser(id))
          replyTo ! Node.SuccessUser(state.toSummaryUser(id))
        Effect.none
//        --------------------------------------------COFFEE--------------------------------------------------------------
      case GetCoffee(id, replyTo) =>
        if (state.hasCoffee(id))
          replyTo ! Node.SuccessCoffee(state.toSummaryCoffee(id))
        Effect.none
      case GetCoffees(replyTo) =>
        replyTo ! Node.SuccessCoffees(state.toSummaryCoffees)
        Effect.none
      case AddCoffee(token, coffee, replyTo) =>
        if(token =="admin"){
          if (state.hasCoffee(coffee.id)) {
            replyTo ! Node.Error(s"Item '${coffee.id}' was already added")
            Effect.none
          } else if (coffee.name == "" || coffee.description == "" || coffee.categoryId == "") {
            replyTo ! Node.Error("Name or description or category mustn't be empty")
            Effect.none
          } else if(state.hasCategory(coffee.categoryId)) {
            Effect
              .persist(CoffeeAdded(cartId, coffee.id, coffee))
              .thenRun(updatedCart => replyTo ! Node.SuccessCoffee(updatedCart.toSummaryCoffee(coffee.id)))
          }else
          {
            replyTo ! Node.Error("Category for book not found!")
            Effect.none
          }
        }else{
          replyTo ! Node.Error("Access denied!")
          Effect.none
        }

      case RemoveCoffee(itemId, replyTo) =>
        if (state.hasCoffee(itemId)) {
          Effect.persist(
            CoffeeRemoved(cartId, itemId)
          ).thenRun(updatedCart => replyTo ! Node.SuccessCoffee(updatedCart.toSummaryCoffee(itemId)))
        } else {
          replyTo ! Node.SuccessCoffees(state.toSummaryCoffees) // removing an item is idempotent
          Effect.none
        }

      case UpdateCoffee(token, coffee, replyTo) =>
        if (state.hasCoffee(coffee.id) && token == "admin") {
          if (coffee.name == "" || coffee.description == "" || coffee.categoryId == "") {
            replyTo ! Node.Error("Name or description mustn't be empty")
            Effect.none
          }else if(state.hasCategory(coffee.categoryId)) {
            Effect
              .persist(CoffeeAdjusted(cartId, coffee.id, coffee))
              .thenRun(updatedCart => replyTo ! Node.SuccessCoffee(updatedCart.toSummaryCoffee(coffee.id)))
          } else{
            replyTo ! Node.Error("There's no such category or admin token expired")
            Effect.none
          }
        }else{
          replyTo ! Node.Error("Access denied!")
          Effect.none
        }
      case RateCoffee(userToken, coffeeId, newRate: Int, replyTo) =>
        if (state.hasCoffee(coffeeId) && userToken!="") {
          if (newRate>=0) {
            Effect
              .persist(CoffeeRated(cartId, coffeeId, newRate))
              .thenRun(updatedCart => replyTo ! Node.SuccessCoffee(updatedCart.toSummaryCoffee(coffeeId)))
        }else{
          replyTo ! Node.Error("Rate must be positive int")
          Effect.none
        }
        }else{
          replyTo ! Node.Error("There's no Coffee")
          Effect.none
        }
      //        --------------------------------------------ORDER--------------------------------------------------------------

      case GetOrder(id, replyTo) =>
        if (state.hasOrder(id))
          replyTo ! Node.SuccessOrder(state.toSummaryOrder(id))
        Effect.none
      case GetOrders(replyTo) =>
        replyTo ! Node.SuccessOrders(state.toSummaryOrders)
        Effect.none



//        --------------------------------------------BOOK--------------------------------------------------------------


//        ------------------------------------------------------CATEGORY------------------------------------------------

      case GetCategory(id, replyTo) =>
        if (state.hasCategory(id))
          replyTo ! Node.SuccessCategory(state.toSummaryCat(id))
        Effect.none
      case GetCategories(replyTo) =>
        replyTo ! Node.SuccessCategories(state.toSummaryCats)
        Effect.none
      case UpdateCategory(token, cat, replyTo) =>
        if (state.hasCategory(cat.id) && token == "admin") {
          if (cat.name == "" || cat.description == "") {
            replyTo ! Node.Error("Name or description mustn't be empty")
            Effect.none
          }else{
            Effect
              .persist(CategoryAdjusted(cartId, cat.id, cat))
              .thenRun(updatedCart => replyTo ! Node.SuccessCategory(updatedCart.toSummaryCat(cat.id)))
          }
        } else{
          replyTo ! Node.Error("There's no such category or admin token expired")
          Effect.none
        }

      case AddCategory(cat, replyTo) =>
        if (state.hasCategory(cat.id)) {
          replyTo ! Node.Error(s"Item '${cat.id}' was already added")
          Effect.none
        } else if (cat.name == "" && cat.description == "") {
          replyTo ! Node.Error("Name or description mustn't be empty")
          Effect.none
        } else {
          Effect
            .persist(CategoryAdded(cartId, cat.id, cat))
            .thenRun(updatedCart => replyTo ! Node.SuccessCategory(updatedCart.toSummaryCat(cat.id)))
        }
      case RemoveCategory(itemId, replyTo) =>
        if (state.hasCategory(itemId)) {
          Effect.persist(
            CategoryRemoved(cartId, itemId)
          ).thenRun(updatedCart => replyTo ! Node.SuccessCategory(updatedCart.toSummaryCat(itemId)))
        } else {
          replyTo ! Node.SuccessCategories(state.toSummaryCats)
          Effect.none
        }







      case GetToken(email, password, replyTo) =>
        state.users.foreach(u => {
          if (u._2.email == email && u._2.password == password)
            replyTo ! Node.Token("user" + u._1)
        })
        Effect.none
    }

  private def checkedOutShoppingCart(cartId: String, state: State, command: Command): Effect[Event, State] =
    command match {
      case GetUser(id, token, replyTo) =>
        if (token == "user" + id && state.hasUser(id))
          replyTo ! Node.SuccessUser(state.toSummaryUser(id))
        Effect.none
      case cmd: AddUser =>
        cmd.replyTo ! Node.Error("Can't add an item to an already checked out account")
        Effect.none
      case cmd: RemoveUser =>
        cmd.replyTo ! Node.Error("Can't remove an item from an already checked out account")
        Effect.none
      case cmd: AdjustUser =>
        cmd.replyTo ! Node.Error("Can't adjust item on an already checked out account")
        Effect.none
      case cmd: Checkout =>
        cmd.replyTo ! Node.Error("Can't checkout already checked out account")
        Effect.none
      case cmd: CheckoutBook =>
        cmd.replyTo ! Node.Error("Can't checkout already checked out account")
        Effect.none
    }

  private def handleEvent(state: State, event: Event) = {
    event match {
      case CheckedOut(_, eventTime) => state.checkout(eventTime)
//        USER ACC
      case UserAdded(_, itemId, quantity) => state.updateUser(itemId, quantity)
      case UserRemoved(_, itemId) => state.removeUser(itemId)
      case UserAdjusted(_, itemId, quantity) => state.updateUser(itemId, quantity)
      case UserCartUpdated(_, coffeeId, userId) => state.updateUser(userId, state.addToCart(coffeeId, state.getUserById(userId)))
//        COFFEE
      case CoffeeAdded(_, itemId, quantity) => state.updateCoffee(itemId, quantity)
      case CoffeeRemoved(_, itemId) => state.removeCoffee(itemId)
      case CoffeeAdjusted(_, itemId, quantity) =>state.updateCoffee(itemId, quantity)
      case CoffeeRated(_, itemId, quantity) =>state.rateCoffee(itemId, quantity)
//        ORDER
//        CATEGORY
      case CategoryAdded(_, itemId, quantity) => state.updateCategory(itemId, quantity)
      case CategoryRemoved(_, itemId) => state.removeCategory(itemId)
      case CategoryAdjusted(_, itemId, quantity) =>state.updateCategory(itemId, quantity)
    }
  }
}
