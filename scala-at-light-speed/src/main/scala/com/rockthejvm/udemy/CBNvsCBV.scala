package com.rockthejvm.udemy

object CBNvsCBV extends App {

  def callByValue(x: Long): Unit = {
    println("by value " + x)
    println("by value " + x)
  }

  def callByName(x: => Long): Unit = {
    println("by name " + x)
    println("by name " + x)
  }

  callByValue(System.nanoTime())
  callByName(System.nanoTime())

  val age:Int = 3
  val str = s"preved medved $age"

  //f interpolator
  val speed = 3.7f
  val name = "medved"
//  val myth = f"$name%s speed is $speed%2.2"

  //raw interpolator
  println(raw"this is a new line \n")
}
