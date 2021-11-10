package com.rockthejvm

import scala.annotation.tailrec

object PatternMatching extends App {
  //switch expression

  val n: Int = 55
  val order = n match {
    case 1 => "first"
    case 2 => "second"
    case _ => "something else"
  }

  case class Person(name: String, age: Int)

  val bob = Person("bob", 42)
  val personGreeting = bob match {
    case Person(n, a) if a < 20 => s"Hi my name is $n and i am $a young"
    case Person(n, a) => s"Hi my name is $n and i am $a old"
    case _ => "something else"
  }

  val tuple = ("Bon Jovi", "rock", "sssss", 123)
  val bandDescripotion = tuple match {
    case (band, genre, name, age) => ""
    case (band, genre, name, 123) => ""
    case (band, genre, _, 123) => ""
    case something => ""
  }

  val list = List(1, 2, 3)
  list match {
    case List(_, 2, _) => "aaaa"
    case List(1, _*) => "aaaa"
  }

  val head :: tail = list
  println("head")
  println(head)
  println(tail)

  val nameBindingMatch = list match {
    case notEmptyList @ List(_, _) => ""
//    case List(_, something @ List(_)) => ""
//    case List(_, something @ List(_)) if something > 2 => ""
  }

  val multiPatternBindingMath = list match {
    case Nil | List(_) => "aaa"
  }

  trait Expr
  case class Number(n: Int) extends Expr
  case class Sum(e1: Expr, e2: Expr) extends Expr
  case class Prod(e1: Expr, e2: Expr) extends Expr

  //Sum(Number(3),Number(3)) => 2 + 3
  def displayInParentheses(expr: Expr): String = {
    expr match {
      case Sum(_,_) => s"(${display(expr)})"
      case _ => display(expr)
    }
  }
  def display(expr: Expr): String = {
    expr match {
      case Number(n) => "" + n
      case Sum(e1, e2) => display(e1) + " + " + display(e2)
      case Prod(e1, e2) =>
        displayInParentheses(e1) + " * " + displayInParentheses(e2)
    }
  }

  println(display(Number(3)))
  println(display(Sum(Number(2), Number(3))))
  println(display(Sum(Sum(Number(2), Number(3)), Number(4))))
  println(display(Prod(Sum(Number(2), Number(3)), Number(4))))
  println(display(Prod(Number(4), Sum(Number(2), Number(3)))))
  println(display(Prod(Number(5), Number(4))))
  println(display(Sum(Prod(Number(5), Number(4)), Number(7))))
}
