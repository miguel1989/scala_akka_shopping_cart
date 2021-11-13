package com.rockthejvm.udemy.basics

object ObjectOrientation extends App {
  class Animal {
    val age: Int = 0

    def eat() = println("i am eating")
  }

  val animal = new Animal

  class Dog(val name: String) extends Animal {

  }

  class Point(x: Int, y: Int)

  val point = new Point(1, 2)

  val dog = new Dog("miguel")
  dog.name

  abstract class WalkingAnimal {
    protected val hasLegs = true //by default public

    def walk(): Unit
  }

  val oneTwoThree = List(1, 2, 3)

  trait Carnivore {
    def eat(animal: Animal): Unit
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(animal: Animal): Unit = println("eating")

    override def eat(): Unit = println("aaa")
  }

  //infix notation -> object method argument
  val aCroc: Crocodile = new Crocodile
  aCroc eat dog
  aCroc.eat(dog)

  val dino = new Carnivore {
    override def eat(animal: Animal): Unit = {
      println("I am a dino")
    }
  }

  object MySingleton {
    val myVal = "aaaa"

    def myMethod(): String = "Abc"

    def apply(x: Int): Int = x + 1
  }

  MySingleton.myMethod()
  MySingleton.apply(12)
  MySingleton(12) //equivalent

  //companion object
  object Animal {
    val canLive = false
  }

  val canAnimalsLive = Animal.canLive //like static in java

  //case classes
  //lightweight data structures
  //auto generate
  // - equals and hashcode
  // - serializion
  // - companion apply
  // - pattern matching !? todo

  case class Person(name: String, age: Int)

  val bob = Person("bob", 42) //instead of new Person. person.apply("bob", 42)

  try {

  } catch {
    case ex: Exception => "some msg"
  }

  //generics
  abstract class MyList[T] {
    def head: T

    def tail: MyList[T]
  }

  val aList: List[Int] = List(1, 2, 3)
  aList.reverse
}
