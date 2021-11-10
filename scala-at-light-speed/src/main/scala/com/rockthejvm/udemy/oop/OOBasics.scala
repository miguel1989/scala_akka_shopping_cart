package com.rockthejvm.udemy.oop

object OOBasics extends App {
  val person = new Person("medved", 12)
  println(person.age)
  person.greet("kreved")
}

class Person(name: String, val age: Int) {

  def greet(name: String): Unit = {
    println(s"${this.name} says: hello $name")
  }

  //multiple constructors
  def this(name: String) = this(name, 0)
}

class Writer(firstName: String, lastName: String, val year: Int) {
  def fullName(): String = {
    s"$firstName $lastName"
  }
}

class Novel(name: String, releaseYear: Int, author: Writer) {
  def authorAge(): Int = {
    releaseYear - author.year
  }

  def isWrittenBy(author: Writer): Boolean = {
    this.author == author
  }

  def copy(newYear: Int): Novel = {
    new Novel(name, newYear, author)
  }
}

class Counter(num: Int) {
  def current(): Int = {
    num
  }

  def inc(n: Int = 1): Counter = {
    new Counter(num + n)
  }

  def dec(n: Int = 1): Counter = {
    new Counter(num - n)
  }
}