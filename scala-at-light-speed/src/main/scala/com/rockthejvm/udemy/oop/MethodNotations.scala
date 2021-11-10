package com.rockthejvm.udemy.oop

import scala.language.postfixOps

object MethodNotations extends App {

  class Person(val name: String, favoriteMovie: String, val age: Int = 0) {
    def likes(movie: String) : Boolean = {
      movie == favoriteMovie
    }

    def +(person: Person):String = s"${this.name} with ${person.name}"
    def +(str:String): Person = new Person(s"${this.name} ($str)", favoriteMovie, age)

    def unary_! : String = s"$name wtf"
    def unary_+ : Person = new Person(name, favoriteMovie, age + 1)
    def isAlive:Boolean = true
    def apply(): String = s"hi $name"
    def apply(n:Int): String = s"hi $name number = $n"
  }

  val mary =  new Person("Mary", "Inception")
  println(mary.likes("Inception"))
  println(mary likes "Inception") //infix notation

  //prefix notation
  val x = -1
  val y = 1.unary_-
  println(!mary)

  //postfix notation
  println(mary isAlive)

  //apply
  println(mary())
}
