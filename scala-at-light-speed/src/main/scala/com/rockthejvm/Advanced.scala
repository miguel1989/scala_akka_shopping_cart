package com.rockthejvm

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object Advanced extends App {
  lazy val aLazyVal = 2
  lazy val lazyWthSideEffect = {
    println("I am lazy")
    32
  }
  val eager = lazyWthSideEffect + 1
  //usefull in inifinte collections

  //pseudo collections
  //Option / Try
  def methodWithNull() : String = "hello"
  val anOption = Option(methodWithNull()) // Some("hello") or None
  val strProc = anOption match {
    case Some(str) => s"hi $str"
    case None => "nothing here"
  }

  def methodThatCanTrow() = throw new RuntimeException
  val aTry = Try(methodThatCanTrow())
  val result = aTry match {
    case Success(value) => "cool"
    case Failure(exception) => "log exception"
  }

  val noNumber: Option[Int] = None
  val result2 = noNumber.map(_ * 1.5)
  println(result2)
  //--------------------------------------------------------------------------------------------
  //Evaulate something on another thread
  //async programming
  val aFuture = Future{
    println("loading ...")
    Thread.sleep(1000)
    println("complete")
    67
  } //monads

  aFuture onComplete {
    case Success(value) => println(value)
    case Failure(ex) => println("Error " + ex.getMessage)
  }
//  println(aFuture.value)


  //--------------------------------------------------------------------------------------------
  //#1 implicit arguments
  def aMethod(implicit arg:Int) = arg + 1
  implicit val myImplVal = 42
  println(aMethod) //aMethod(myImplVal)

  //#2 implicit conversions
  implicit class RichInt(n:Int) {
    def isEven() = n % 2
  }
  println(23.isEven())
}

