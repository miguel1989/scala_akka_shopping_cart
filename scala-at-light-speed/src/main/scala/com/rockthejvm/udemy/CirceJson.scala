package com.rockthejvm.udemy

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.parser.{decode, parse}
import io.circe.{Decoder, Encoder, Error}

object CirceJson extends App {

  //Option[String]
  case class Person(name: String, age: Int)

  val person = Person("Miguel", 32)

  val jsonStr = Encoder[Person].apply(person).spaces2SortKeys

  println("-" * 100)
  println(jsonStr)
  //  println(person.asJson) //circe.syntax

  val str = {
    """
    {
      "name": "medved",
      "age": 32
    }
    """
  }
  val decoded: Either[Error, Person] = decode[Person](str)
  val decoded2: Either[Error, Person] = parse(str).flatMap(Decoder[Person].decodeJson)
  val decoded3: Either[Error, Person] = parse(str).flatMap(_.as[Person])
  println("-" * 100)
  println(decoded)
}
