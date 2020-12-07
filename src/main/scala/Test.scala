import model.Coffee

object Test extends App {


  val temp = new Coffee("id","id2","name", "desc", Array.empty)

  def test(): Unit ={

    val newList = temp.rating :+ 1
    val coffee = temp.copy(rating = newList)

    println(coffee.rating.size)

  }

  println(test())
}
