package com.rockthejvm

object PatternMatching extends App {
  //switch expression

  val n: Int = 55
  val order = n match {
    case 1 => "first"
    case 2 => "second"
    case _ => "something else"
  }

  case class Person(name:String, age:Int)
  val bob = Person("bob", 42)
  val personGreeting = bob match {
    case Person(n,a) => s"Hi my name is $n and i am $a old"
    case _ => "something else"
  }

  val tuple = ("Bon Jovi", "rock", "sssss", 123)
  val bandDescripotion = tuple match {
    case (band, genre, name, age) => ""
  }

  val list = List(1,2,3)
  list match {
    case List(_, 2, _) => "aaaa"
  }
}
