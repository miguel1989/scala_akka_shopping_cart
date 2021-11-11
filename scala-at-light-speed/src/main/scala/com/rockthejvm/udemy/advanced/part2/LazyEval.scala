package com.rockthejvm.udemy.advanced.part2

object LazyEval extends App {
  lazy val x = 4

  def sideEffectConditions: Boolean = {
    println("ffff")
    true
  }

  def simpleCondition = false

  lazy val lazyCondition = sideEffectConditions
  println(if (simpleCondition && lazyCondition) " yes" else "no")


  //filtering with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s" $i is less than 30")
    i < 30
  }

  def greaterThan20(i: Int): Boolean = {
    println(s" $i is greater than 20")
    i > 20
  }

  val numbers = List(1, 25, 40, 5, 23)
  val lt30 = numbers.filter(lessThan30)
  val gt20 = lt30.filter(greaterThan20)
  println(gt20)

  val lt30Lazy = numbers.withFilter(lessThan30) //uses lazy values
  val gt20Lazy = lt30Lazy.withFilter(greaterThan20)
  println(gt20Lazy)
  gt20Lazy.foreach(println)
}
