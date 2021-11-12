package com.rockthejvm.udemy.advanced.part4

object Implicits extends App {

  val pair = "Danial" -> "555"

  case class Person(name:String) {
    def greet = s"hi my name is $name"
  }

  implicit def fromStrToPerson(str: String) : Person = Person(str)

  println("peter".greet)

  def increment(x:Int)(implicit amount: Int) = x + amount
  implicit val defAmount: Int = 10
  increment(2)

  implicit val reverseOrder: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(3,4,2,1).sorted)



  case class Purchase(nUnits: Int, price: Double)
  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a,b) => a.nUnits * a.price < b.nUnits * b.price)
  }
  object ByUnitOrdering {
    implicit val byUnitOrdering: Ordering[Purchase] = Ordering.fromLessThan((a,b) => a.nUnits < b.nUnits)
  }
  object ByPriceOrdering {
    implicit val byPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a,b) => a.price < b.price)
  }
}
