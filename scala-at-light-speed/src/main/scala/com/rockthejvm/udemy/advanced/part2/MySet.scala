package com.rockthejvm.udemy.advanced.part2

import scala.annotation.tailrec

trait MySet[A] extends (A => Boolean){

  def apply(elem: A): Boolean = contains(elem)
  def contains(elem: A): Boolean
  def +(elem: A): MySet[A]
  def ++(set: MySet[A]): MySet[A]
  def -(elem: A): MySet[A]

  def --(set: MySet[A]): MySet[A]
  def &(set: MySet[A]): MySet[A]

  def map[B](f:A => B): MySet[B]
  def flatMap[B](f:A => MySet[B]): MySet[B]
  def filter(f: A => Boolean): MySet[A]
  def foreach(f: A=> Unit): Unit

  def unary_! : MySet[A]
}

class MyEmptySet[A] extends MySet[A] {
  override def contains(elem: A): Boolean = false

  override def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)

  override def ++(set: MySet[A]): MySet[A] = set

  def -(elem: A): MySet[A] = this

  def --(set: MySet[A]): MySet[A] = this
  def &(set: MySet[A]): MySet[A] = this

  override def map[B](f: A => B): MySet[B] = new MyEmptySet[B]

  override def flatMap[B](f: A => MySet[B]): MySet[B] = new MyEmptySet[B]

  override def filter(f: A => Boolean): MySet[A] = this

  override def foreach(f: A => Unit): Unit = ()

  def unary_! : MySet[A] = new PropertyBasedSet[A](_ => true)
}

//all elements of type A which satisfy prop
class PropertyBasedSet[A](prop: A => Boolean) extends MySet[A] {
  override def contains(elem: A): Boolean = prop(elem)
  override def +(elem: A): MySet[A] = new PropertyBasedSet[A](x => prop(x) || x == elem)
  override def ++(set: MySet[A]): MySet[A] = new PropertyBasedSet[A](x => prop(x) || set(x))
  override def -(elem: A): MySet[A] = filter(x => x != elem)
  override def --(set: MySet[A]): MySet[A] = filter(!set)
  override def &(set: MySet[A]): MySet[A] = filter(set)
  override def map[B](f: A => B): MySet[B] = politeFail
  override def flatMap[B](f: A => MySet[B]): MySet[B] = politeFail
  override def filter(f: A => Boolean): MySet[A] = new PropertyBasedSet[A](x => prop(x) && f(x))
  override def foreach(f: A => Unit): Unit = politeFail
  override def unary_! : MySet[A] = new PropertyBasedSet[A](x => !prop(x))

  def politeFail = throw new RuntimeException("deep rabbit hole")
}

class NonEmptySet[A](val head: A, tail: MySet[A]) extends MySet[A] {
  override def contains(elem: A): Boolean = head == elem || tail.contains(elem)

  override def +(elem: A): MySet[A] = {
    if (this.contains(elem)) this
    else new NonEmptySet[A](elem, this)
  }

  override def ++(set: MySet[A]): MySet[A] = tail ++ set + head

  def -(elem: A): MySet[A] = {
    if (head == elem) tail
    else tail - elem + head
  }

  def --(set: MySet[A]): MySet[A] = filter(x => !set.contains(x))

//  def &(set: MySet[A]): MySet[A] = filter(x => set.contains(x)) //or set(x)
  def &(set: MySet[A]): MySet[A] = filter(set)

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

  def unary_! : MySet[A] = new PropertyBasedSet[A](x => !this.contains(x))
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
  val mySet2 = mySet + 7

//  mySet.map(x => x * 4).foreach(println)
  mySet2.foreach(println)
  (mySet2 - 2).foreach(println)
}