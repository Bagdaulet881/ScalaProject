package model


case class Coffee(id: String,categoryId:String, name: String, description:String, rating: Array[Int])
case class CreateCoffee(token: String,categoryId:String, name: String, description:String)

case class UpdateCoffee(token:String,categoryId:String, name: String, description: String, rating: Array[Int])
case class RateCoffee(userToken:String,coffeeId:String, rating: String)
case class DeleteCoffee(id: String)




//case class Category(id: String, name: String, description:String)
//case class CreateCategory(token: String, name: String, description:String)
//
//case class UpdateCategory(token:String, name: String, description: String)
//case class DeleteCategory(id: String)

