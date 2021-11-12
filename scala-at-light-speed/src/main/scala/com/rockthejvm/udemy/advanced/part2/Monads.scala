package com.rockthejvm.udemy.advanced.part2

object Monads extends App {

  trait Attempt[+A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
  }

  object Attempt {
    def apply[A](a: => A): Attempt[A] =
      try {
        Success(a)
      } catch {
        case e: Throwable => Failure(e)
      }
  }

  case class Success[+A](value: A) extends Attempt[A] {
    override def flatMap[B](f: A => Attempt[B]): Attempt[B] =
      try {
        f(value)
      } catch {
        case e: Throwable => Failure(e)
      }
  }

  case class Failure(e: Throwable) extends Attempt[Nothing] {
    override def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = this
  }

  class Lazy[T](arg: => T) {
    private lazy val internalValue = arg
    def use: T = internalValue
    def flatMap[R](f: (=> T) => Lazy[R]): Lazy[R] = {
      println("inside lazy flatMap")
      f(internalValue)
    }
  }
  object Lazy {
    def apply[T](arg: => T): Lazy[T] = new Lazy(arg)
  }

  val myLazy = Lazy{
    println("Lazy init")
    42
  }

  println(myLazy.flatMap(x => Lazy(10*x)))
}
