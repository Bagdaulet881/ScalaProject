package model

case class User(id: String, email: String, password:String, books: Seq[Book])
case class CreateUser(email: String, password:String)
case class AddBookToUser(bookId: String, userId:String, userToken:String)

case class SignIn(email: String, password: String)
case class Token(token: String)
