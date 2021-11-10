package com.rockthejvm.udemy.fp

object fp extends App {

  val concatenator = (str1: String, str2: String) => str1 + str2

  println(concatenator("ABC", "DEFG"))

  //curry
  val ff = (x: Int) => (y: Int) => x * y
  println(ff(2)(3))


  val niceInc: Int => Int = _ + 1
  val niceAdd: (Int, Int) => Int = _ + _

  def nTimes(f: Int => Int, n: Int, x: Int): Int = {
    if (n <= 0) x
    else {
      nTimes(f, n - 1, f(x))
    }
  }

  val plusOne = (x: Int) => x + 1
  val plusTwo = (x: Int) => x + 2
  val multiplyTwo = (x: Int) => x * 2

  def nTimesBetter(f: Int => Int, n: Int): Int => Int = {
    if (n < 0) {
      (x: Int) => x
    } else {
      (x: Int) => nTimesBetter(f, n - 1)(f(x))
    }
  }

  val plusTen = nTimesBetter(plusOne, 10)

  println(nTimes(plusOne, 3, 1))
  println(plusTen(1))


  def toCurry(f: (Int, Int) => Int): Int => Int => Int = {
//    f.curried
    x => y => f(x,y)
  }

  def fromCurry(f: Int => Int => Int) : (Int,Int) => Int = {
    (x,y) => f(x)(y)
  }

  def compose(f1: Int => Int, f2: Int => Int): Int => Int = {
    (x: Int) => f1(f2(x))
  }
  def compose2(f1: Int => Int, f2: Int => Int): Int => Int = {
    (x: Int) => f2(f1(x))
  }

  println("-------------")
  println(toCurry(niceAdd)(1)(3))
  println(compose(plusTwo, multiplyTwo)(2))
  println(compose2(plusTwo, multiplyTwo)(2))
}
