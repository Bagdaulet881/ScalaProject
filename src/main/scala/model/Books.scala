package model

case class Book(id: String,categoryId:String, name: String, description:String)
case class CreateBook(token: String,categoryId:String, name: String, description:String)

//TODO categoryId:String,
case class UpdateBook(token:String,categoryId:String, name: String, description: String)
case class Delete(id: String)




case class Category(id: String, name: String, description:String)
case class CreateCategory(token: String, name: String, description:String)

case class UpdateCategory(token:String, name: String, description: String)
case class DeleteCategory(id: String)

