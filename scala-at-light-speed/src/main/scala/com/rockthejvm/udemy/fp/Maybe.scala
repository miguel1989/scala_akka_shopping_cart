package com.rockthejvm.udemy.fp

abstract class Maybe[+T] {
  def map[A](f: T => A): Maybe[A]

  def flatMap[A](f: T => Maybe[A]): Maybe[A]

  def filter(p: T => Boolean): Maybe[T]
}

case object MaybeNot extends Maybe[Nothing] {
  override def map[A](f: Nothing => A): Maybe[A] = MaybeNot

  override def flatMap[A](f: Nothing => Maybe[A]): Maybe[A] = MaybeNot

  override def filter(p: Nothing => Boolean): Maybe[Nothing] = MaybeNot
}

case class Just[+T](elem: T) extends Maybe[T] {
  override def map[A](f: T => A): Maybe[A] = Just(f(elem))

  override def flatMap[A](f: T => Maybe[A]): Maybe[A] = f(elem)

  override def filter(p: T => Boolean): Maybe[T] = {
    if (p(elem)) this
    else MaybeNot
  }
}

object MaybeTest extends App {
  val just3 = Just(3)

  println(just3)
  println(just3.map(_ * 2))
  println(just3.flatMap(x => Just(x % 2 == 0)))
  println(just3.filter(x => x % 2 == 0))
}

