package model

case class User(id: String, email: String, password:String, orders: Seq[Coffee])
case class CreateUser(email: String, password:String)
case class AddCoffeeToUserOrderList(coffeeId: String, userToken:String)

case class SignIn(email: String, password: String)
case class Token(token: String)
