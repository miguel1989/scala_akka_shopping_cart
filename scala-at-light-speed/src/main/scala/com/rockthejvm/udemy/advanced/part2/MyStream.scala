package com.rockthejvm.udemy.advanced.part2

import scala.annotation.tailrec

abstract class MyStream[+A] {
  def isEmpty: Boolean

  def head: A

  def tail: MyStream[A]

  def #::[B >: A](element: B): MyStream[B] //prepend

  def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B]

  def foreach(f: A => Unit): Unit

  def map[B](f: A => B): MyStream[B]

  def flatMap[B](f: A => MyStream[B]): MyStream[B]

  def filter(p: A => Boolean): MyStream[A]

  def take(n: Int): MyStream[A] //takes first n elements

  def takeAsList(n: Int): List[A] //takes first n elements

  @tailrec
  final def toList[B >: A](acc: List[B] = Nil): List[B] =
    if (isEmpty) acc.reverse
    else tail.toList(head :: acc)
}

class EmptyStream extends MyStream[Nothing] {
  override def isEmpty: Boolean = true

  override def head: Nothing = throw new RuntimeException("nothing")

  override def tail: MyStream[Nothing] = throw new RuntimeException("nothing")

  override def #::[B >: Nothing](element: B): MyStream[B] = new NonEmptyStream[B](element, this)

  override def ++[B >: Nothing](anotherStream: => MyStream[B]): MyStream[B] = anotherStream

  override def foreach(f: Nothing => Unit): Unit = ()

  override def map[B](f: Nothing => B): MyStream[B] = this

  override def flatMap[B](f: Nothing => MyStream[B]): MyStream[B] = this

  override def filter(p: Nothing => Boolean): MyStream[Nothing] = this

  override def take(n: Int): MyStream[Nothing] = this

  override def takeAsList(n: Int): List[Nothing] = Nil
}

class NonEmptyStream[+A](hd: A, tl: => MyStream[A]) extends MyStream[A] {
  override def isEmpty: Boolean = false

  override val head: A = hd

  override lazy val tail: MyStream[A] = tl

  override def #::[B >: A](element: B): MyStream[B] = new NonEmptyStream(element, this)

  override def ++[B >: A](anotherStream: => MyStream[B]): MyStream[B] = new NonEmptyStream(head, tail ++ anotherStream)

  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  override def map[B](f: A => B): MyStream[B] = new NonEmptyStream(f(head), tail.map(f))

  override def flatMap[B](f: A => MyStream[B]): MyStream[B] = f(head) ++ tail.flatMap(f)

  override def filter(p: A => Boolean): MyStream[A] =
    if (p(head)) new NonEmptyStream(head, tail.filter(p))
    else tail.filter(p)

  override def take(n: Int): MyStream[A] = {
    if (n <= 0) new EmptyStream
    if (n <= 1) new NonEmptyStream(head, new EmptyStream)
    else new NonEmptyStream(head, tail.take(n - 1))
  }

  override def takeAsList(n: Int): List[A] = take(n).toList()
}

object MyStream {
  def from[A](start: A)(generator: A => A): MyStream[A] =
    new NonEmptyStream(start, MyStream.from(generator(start))(generator))
}

object testStream extends App {
  val naturals = MyStream.from(1)(_ + 1)
  println(naturals.head)
  println(naturals.tail.head)
  println(naturals.tail.tail.head)

  val startFrom0 = 0 #:: naturals

  println(naturals.takeAsList(100))
  naturals.map(_ * 2).take(10).foreach(println)
}