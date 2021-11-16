package com.rockthejvm.udemy

object CatsIntro extends App {
  import cats.Eq
  import cats.instances.int._

  val intEquality = Eq[Int]
  val aTypeSafeComparison = intEquality.eqv(1,2)
  //use extension methods if applicable
  import cats.syntax.eq._
  val compare = 1 === 3

  //extending TC operations to composite types
//  implicit cats.instances.list //we bring Eq[List[Int]]
  val aListComparison = List(1) === List(2)
}
