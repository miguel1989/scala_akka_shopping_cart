package com.rockthejvm.udemy.fp

import scala.util.{Failure, Random, Success, Try}

object Failures extends App {

  val aSuccess = Success(3)
  val aFailure = Failure(new RuntimeException("AAA"))

  println(aSuccess)
  println(aFailure)

  def unsafeMethod(): String = {
    throw new RuntimeException("BBB")
  }

  val potentialFailure = Try(unsafeMethod())
  println(potentialFailure)

  val potentialFailure2 = Try {
    //do code
  }

  Try(unsafeMethod()).orElse(Try { //something else
  })

  println(aSuccess.map(_ * 2))
  println(aSuccess.flatMap(x => Success(x * 10)))
  println(aSuccess.filter(_ > 10))


  val hostname = "localhost"
  val port = "8080"

  def renderHtml(page: String) = {
    println(page)
  }

  class Connection {
    def get(url: String): String = {
      val random = new Random(System.nanoTime())
      if (random.nextBoolean()) {
        "<html></html>"
      } else {
        throw new RuntimeException("Fail get url")
      }
    }

    def getSafe(url: String): Try[String] = Try(get(url))
  }

  object HttpService {
    val random = new Random(System.nanoTime())

    def getConnection(host: String, port: String): Connection = {
      if (random.nextBoolean()) new Connection
      else throw new RuntimeException("AAA")
    }

    def getConSafe(host: String, port: String): Try[Connection] = Try(getConnection(host, port))
  }

  val exercise1 = Try(HttpService.getConnection(hostname, port)).flatMap(c => Try(c.get("someUrl")).map(str => renderHtml(str)))
  HttpService.getConSafe(hostname, port).flatMap(c => c.getSafe("url")).foreach(renderHtml)

  val exercise2 = for {
    con <- Try(HttpService.getConnection(hostname, port))
    str <- Try(con.get("url"))
  } yield renderHtml(str)
}
