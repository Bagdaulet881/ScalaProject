package model


case class Coffee(id: String,categoryId:String, name: String, description:String, rating: Array[Int])
case class CreateCoffee(token: String,categoryId:String, name: String, description:String)

case class UpdateCoffee(token:String,categoryId:String, name: String, description: String)
case class RateCoffee(userToken:String, rating: String)
case class DeleteCoffee(id: String)





