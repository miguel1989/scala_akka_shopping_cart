package com.rockthejvm.udemy.advanced.part2

object PartialFunction extends App {

  val func = (x:Int) => x + 1

  val partFunc: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 72
  }
  val partFunc2: PartialFunction[Int, Int] = {
    case 5 => 555
    case _ => 777
  }

  println(partFunc(2))
//  println(partFunc(4)) ERROR

  //PF Utils
  println(partFunc.isDefinedAt(4))
  val lifted = partFunc.lift // Int => Option[Int]
  println(lifted(2))
  println(lifted(4))
  val chained = partFunc.orElse(partFunc2)
  println(chained(2))
  println(chained(5))


  //HOFs accept partial functions as well
  val listRes = List(1,2).map(partFunc)
  println(listRes)


  val anonimusPartFunc = new PartialFunction[Int, String]() {
    override def isDefinedAt(x: Int): Boolean = x < 10
    override def apply(v1: Int): String = v1 match {
      case 1 => "low"
      case 2 => "low"
      case _ => "high"
    }
  }
  println(anonimusPartFunc(1))
  println(anonimusPartFunc(7))
  println(anonimusPartFunc(20))
}
