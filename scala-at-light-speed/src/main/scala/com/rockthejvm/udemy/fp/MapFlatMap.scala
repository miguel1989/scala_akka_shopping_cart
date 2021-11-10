package com.rockthejvm.udemy.fp

object MapFlatMap extends App {

  val numbers = List(1,2,3)
  val chars = List('a','b','c')
  val colors = List("black", "white")

  println(numbers.flatMap(num => chars.map(char => s"$num-$char")))

  val combinations = for {
    num <- numbers if num % 2 == 0
    char <- chars
    color <- colors
  } yield s"$num$char-$color"

  println(combinations)
}
