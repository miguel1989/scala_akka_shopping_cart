package com.rockthejvm.udemy.advanced.part4

import org.w3c.dom.html.HTMLScriptElement

object TypeClasses extends App {
  trait HTMLWritable {
    def toHtml: String
  }

  case class User(name: String, age:Int, email:String) extends HTMLWritable {
    override def toHtml: String = s"<div>$name with $age $email</div>"
  }

  val john = User("John", 32, "medved@gmail.com")

  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  object HTMLSerializer {
    def serialize[T](value : T)(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
    def apply[T](implicit serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }

  object UserSerializer extends HTMLSerializer[User] {
    override def serialize(value: User): String = s"<div>${value.name} with ${value.age} ${value.email}</div>"
  }
  object PartialUserSerializer extends HTMLSerializer[User] {
    override def serialize(value: User): String = s"<div>${value.name}</div>"
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div>${value}</div>"
  }

  println(UserSerializer.serialize(john))
  println(HTMLSerializer.serialize(42))
  println(HTMLSerializer[Int].serialize(33))
}
