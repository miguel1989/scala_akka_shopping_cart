package com.rockthejvm.udemy.advanced.part1

object AdvancedPatternMatching extends App {

  val numbers = List(1)
  val desc = numbers match {
    case head :: Nil => println("only 1 element")
    case _ => println("many elements")
  }

  class Person(val name: String, val age:Int)
  object Person {
    def unapply(person: Person): Option[(String, Int)] = Some(person.name, person.age)
  }

  object even {
    def unapply(n:Int): Option[Boolean] =
      if (n % 2 == 0) Some(true)
      else None
  }
  object evenBetter {
    def unapply(n:Int): Boolean =
      if (n % 2 == 0) true
      else false
  }

  val n:Int = 45
  val str = n match {
//    case even(_) => "is even"
    case evenBetter() => "is even"
    case _ => "something"
  }

  //infix patterns
  case class Or[A,B](a:A, b:B)
  val either = Or(2,"two")
  val humanStr = either match {
    case num Or str => s"$num in words is $str"
  }


  //decomposing seq
  val vararg = numbers match {
    case List(1, _*) => "starting with 1"
  }

//  object MyList {
//    def unapplySeq[A](list: MyList[A]): Option[Seq[A]]
//  }

  //custom return types for unapply
  abstract class Wrapper[A] {
    def isEmpty: Boolean
    def get: A
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      def isEmpty: Boolean = false
      def get: String = person.name
    }
  }

  val bob = new Person("bob", 42)
  val something = bob match {
    case PersonWrapper(str) => s"name is $str"
  }
}
