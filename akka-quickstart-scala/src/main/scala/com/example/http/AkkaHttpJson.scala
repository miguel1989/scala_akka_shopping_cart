package com.example.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import java.util.UUID
//import io.circe._
import spray.json._

case class Person(name: String, age: Int)

case class UserAdded(id: String, timestamp: Long, name: String)

trait PersonJsonProtocol extends DefaultJsonProtocol {
  implicit val personFormat = jsonFormat2(Person)
  implicit val userAddedFormat = jsonFormat3(UserAdded)
}

object AkkaHttpJson extends PersonJsonProtocol with SprayJsonSupport {

  implicit val system = ActorSystem(Behaviors.empty, "server")

  val route: Route = (path("api" / "user") & post) {
    //    complete("medved")
    entity(as[Person]) { person: Person =>
      complete(UserAdded(UUID.randomUUID().toString, System.currentTimeMillis(), person.name))
    }
  }

  def main(args: Array[String]): Unit = {
    Http().newServerAt("localhost", 8081).bind(route)
  }
}

object AkkaHttpCirce extends FailFastCirceSupport {
  import io.circe.generic.auto._ //implicit encoders and decoders

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "server")

  val route: Route = (path("api" / "user") & post) {
    entity(as[Person]) { person: Person =>
      complete(UserAdded(UUID.randomUUID().toString, System.currentTimeMillis(), person.name))
    }
  }

  def main(args: Array[String]): Unit = {
    Http().newServerAt("localhost", 8081).bind(route)
  }
}
