package com.rockthejvm.udemy.advanced.part2

object Curries extends App {

  def curriedAdder(x:Int)(y: Int): Int = x + y
  val add4: Int => Int = curriedAdder(4)
//  curriedAdder(4)
  //functions !+ methods

  val add5 = curriedAdder(5) _


  val simpleAddFunc = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedAddMethod(x:Int)(y: Int) = x + y

  val add7 = simpleAddFunc(7 , _)
  val add7_1 = (x:Int) => simpleAddFunc(7, x)
  val add7_2 = simpleAddFunc.curried(7)
  val add7_3 = curriedAddMethod(7) _ //PAF partial applied function
  val add7_4 = curriedAddMethod(7)(_) //PAF alternative
  val add7_5 = simpleAddMethod(7, _)
  println(add7(2))

  def concatenator(a:String, b: String, c:String) = a + b + c
  val insertName = concatenator("hi ", _, " medved")
  println(insertName("kreved"))


  def formatNumber(format: String)(num:Double): String = {
    format.format(num)
  }
  val shortFormat = (num: Double) => formatNumber("%4.2f")(num)
  val mediumFormat = formatNumber("%8.6f") _
  val longFormat =  formatNumber("%14.12f") _
  val numbers = List(Math.PI, Math.E, 1)

  println(numbers.map(mediumFormat))
  println(numbers.map(formatNumber("%4.2f")))

  def byName(n: => Int) = n + 1
  def byFunc(f:() => Int) = f() + 1
}
