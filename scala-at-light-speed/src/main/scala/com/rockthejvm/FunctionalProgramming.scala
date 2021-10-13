package com.rockthejvm

object FunctionalProgramming extends App {
  class Person(name: String) {
    def apply(age:Int): Unit = println(s" aged $age")
  }
  val bob = new Person("bob")
  bob(42)
  bob.apply(42)

  /*
  Funtional programming
  - compose functions
  - pass functions as args
  - return functions
   */

  //Function_X type
  val simpleIncrementor = new Function1[Int, Int] {
    override def apply(arg: Int): Int = arg + 1
  }
  simpleIncrementor(42)

  val simpleConcatenator = new ((String, String) => Int) {
    override def apply(arg1: String, arg2: String): Int = arg1.length + arg2.length
  }
  simpleConcatenator("aa", "bc")
  val simpleConcatenator2: (String, String) => Int = (x:String, y:String) => x.length + y.length
  val simpleConcatenator3 = (x:String, y:String) => x.length + y.length

  val doubler: Function1[Int, Int] = (x:Int) => x * 2
  val doubler2: Int => Int = (x:Int) => x * 2
  val doubler3 = (x:Int) => x * 2

  val mappedList = List(1,2,3).map(x => x * 2)
  val flatMappedList = List(1,2,3).flatMap(x => List(x, x * 2))
  List(1,2,3).filter(x => x % 2 > 0)
  List(1,2,3).filter(_ <= 3)

  val allPairs = List(1,2,3).flatMap(num => List('a', 'b', 'c').map(letter => s"$num-$letter"))
  //for comprehensions
  val alternativePairs = for {
    number <- List(1,2,3)
    letter <- List('a', 'b', 'c')
  } yield s"$number-$letter"


  //higher order function
  val myF = (x:Int, y:Int) => x + y
  def summation = (a:Int, ff: (Int, Int) => Int) => ff(a, 1)

  def addWithoutSyntaxSugar(x: Int): Function1[Int, Int] = {
    new Function1[Int, Int]() {
      def apply(y: Int): Int = x + y
    }
  }

  def addWithoutSyntaxSugar2(x: Int): (Int) => Int = {
    new ((Int) => Int)() {
      def apply(y: Int): Int = x + y
    }
  }
  def addWithSyntaxSugar(x: Int) = (y: Int, z:Int) => x + y + z

  println("addWithoutSyntaxSugar")
  println(addWithoutSyntaxSugar(1)(2))
  println(addWithSyntaxSugar(1)(2,3))


  //Collections
  val aList = List(1,2,3,4,5)
  val aPrependedList = 0 :: aList
  val anExtendedList = 0 +: aList :+ "61123"

  val aSequence: Seq[Int] = Seq(1,2,3) // Seq.apply(1,2,3)
  val elem = aSequence(1)

  //fast sequence
  val aVector = Vector(1,2,3,4,5)

  val aSet = Set(1,2,3,4,1,2)
  aSet.contains(5)
  val addedSet = aSet + 5
  val removedSet = aSet - 4

  val aRange = 1 to 1000
  val twoByTwo = aRange.map(x => x * 2).toList

  //tuples - group of values
  val tuple = ("Bonm Jove", "Rock", 1928)

  val aPhoneBook: Map[String, Int] = Map(
    ("Daniel", 123),
    "John" -> 321
  )
}
