package com.rockthejvm.udemy.fp

import scala.util.Random

object Collections extends App {

  //Sequences
  val seq = Seq(1, 2, 3)
  val seq2 = seq ++ Seq(4, 5, 6)

  println(seq)
  println(seq.reverse)
  println(seq(1))

  //Ranges
  val range = 1 to 10 //1 until 10
  range.foreach(println)

  //Lists
  val list = List(1, 2, 3)
  val prepended = 43 +: list :+ 89
  println(list ++ List(4, 5, 6))
  println(list.mkString("--"))

  //Arrays
  val numbers = Array(1, 2, 3)
  val threeElems = Array.ofDim[Int](3)
  println(threeElems)
  threeElems.foreach(println)

  //Vectors
  val vector = Vector(1, 2, 3)

  val maxRuns = 1000
  val maxCapacity = 1000000

  def getWriteTime(col: Seq[Int]): Double = {
    val r = new Random
    val times = for {
      it <- 1 to maxRuns
    } yield {
      val curTime = System.nanoTime()
      col.updated(r.nextInt(maxCapacity), 0)
      System.nanoTime() - curTime
    }

    times.sum * 1.0 / maxRuns
  }

  val numberList = (1 to maxCapacity).toList
  val numberVector = (1 to maxCapacity).toVector
  //  println(getWriteTime(numberList))
  //  println(getWriteTime(numberVector))


  //Tuple
  val aTuple = Tuple2(2, "medved")
  val aTuple2 = (2, "medved")
  println(aTuple._1)
  println(aTuple.copy(_2 = "kreved"))
  println(aTuple.swap)

  //Maps
  val aMap: Map[String, Int] = Map(("abc", 1), "fff" -> 2).withDefaultValue(-1)

  println(aMap.map(pair => pair._1.toUpperCase))
  println(aMap.view.filterKeys(k => k.startsWith("a")))

  val names = List("Bob", "Daniel", "Mary", "Michael", "Dan", "Benjamin")
  println(names.groupBy(it => it.charAt(0)))


  val config: Map[String, String] = Map(
    "host" -> "ip",
    "port" -> "8090"
  )

  class Connection {
    def connect = "connected"
  }

  object Connection {
    val random = new Random(System.nanoTime())

    def apply(host: String, port: String): Option[Connection] = {
      if (random.nextBoolean()) Some(new Connection)
      else None
    }
  }

  config.get("host").flatMap(h => config.get("port").flatMap(p => Connection(h,p)))
  Connection("1", "2") match {
    case Some(c) => println(c.connect)
    case None => println("fail")
  }

  //-----------------------------------------------------------------
  class Network() {
    var network: Map[String, Set[String]] = Map()

    def add(name: String): Unit = {
      network + name -> Seq()
    }

    def remove(name: String): Unit = {
      network -= name
    }

    def friend(name1: String, name2: String): Boolean = {
      if (network.contains(name1)) {
        network.get(name1) + name2
        true
      } else false
    }

    def unfriend(name1: String, name2: String): Boolean = {
      if (network.contains(name1)) {
        network(name1) - name2
        true
      } else false
    }

    def friendCount(name: String): Int = {
      if (network.contains(name)) {
        network(name).size
      } else -1
    }

    def mostFriends(): String = {
      val max = network.maxBy(pair => {
        pair._2.size
      })
      max._1
    }

    def countNoFriends(): Int = {
      network.count(pair => {
        pair._2.isEmpty
      })
    }
  }
}
