package com.rockthejvm.udemy.advanced.part2

import scala.annotation.tailrec

trait MySet[A] extends (A => Boolean){

  def apply(elem: A): Boolean = contains(elem)
  def contains(elem: A): Boolean
  def +(elem: A): MySet[A]
  def ++(set: MySet[A]): MySet[A]

  def map[B](f:A => B): MySet[B]
  def flatMap[B](f:A => MySet[B]): MySet[B]
  def filter(f: A => Boolean): MySet[A]
  def foreach(f: A=> Unit): Unit
}

class MyEmptySet[A] extends MySet[A] {
  override def contains(elem: A): Boolean = false

  override def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)

  override def ++(set: MySet[A]): MySet[A] = set

  override def map[B](f: A => B): MySet[B] = new MyEmptySet[B]

  override def flatMap[B](f: A => MySet[B]): MySet[B] = new MyEmptySet[B]

  override def filter(f: A => Boolean): MySet[A] = this

  override def foreach(f: A => Unit): Unit = ()
}

class NonEmptySet[A](val head: A, tail: MySet[A]) extends MySet[A] {
  override def contains(elem: A): Boolean = head == elem || tail.contains(elem)

  override def +(elem: A): MySet[A] = {
    if (this.contains(elem)) this
    else new NonEmptySet[A](elem, this)
  }

  override def ++(set: MySet[A]): MySet[A] = tail ++ set + head

  override def map[B](f: A => B): MySet[B] = tail.map(f) + f(head)

  override def flatMap[B](f: A => MySet[B]): MySet[B] = f(head) ++ tail.flatMap(f)

  override def filter(p: A => Boolean): MySet[A] = {
    val filteredTail = tail.filter(p)
    if (p(head)) filteredTail + head
    else filteredTail
  }

  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }
}

object MySet {
//  def apply[A](values: A*): MySet[A] = {
//    @tailrec
//    def buildSet(seq: Seq[A], acc: MySet[A]): MySet[A] = {
//      if (seq.isEmpty) acc
//      else buildSet(seq.tail, acc + seq.head)
//    }
//
////    buildSet(values.toSeq , new MyEmptySet[A])
//  }
}

object test extends App {
  val mySet = new NonEmptySet[Int](3, new MyEmptySet[Int])

  mySet.map(x => x * 4).foreach(println)
}