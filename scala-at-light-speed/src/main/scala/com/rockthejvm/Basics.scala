package com.rockthejvm

object Basics extends App {
  val m:Int = 42
  val b = false
  val str: String = "Medved"
  val interpolated: String = s"the str is $str"

  val ifExpr = if (m > 43) 56 else 999

  val aCodeBlock = {
    val aLocal = 67
    var b:Int = 1

    aLocal + 2 + b
  }

  def myFync(x: Int, y:String): String = {
    y + " " + x
  }

  def factorial(n:Int):Int = {
    if (n <= 1) {
      return 1
    }
    n * factorial(n -1)
  }
}
