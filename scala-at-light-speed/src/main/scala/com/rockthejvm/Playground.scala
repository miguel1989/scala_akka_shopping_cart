package com.rockthejvm

import scala.collection.immutable.LazyList.cons

object Playground extends App {
  val text = "preved medved kak dela medved kak"

  var countMap: Map[String, Int] = Map()
  text.split("\\s").foreach(word => {
    if (countMap.contains(word)) {
      val newCount = countMap(word) + 1
      countMap = countMap + (word -> newCount)
    }
    else {
      countMap = countMap + (word -> 1)
    }
  })

  println(countMap)
  //println(text.split("\\s").groupBy(word => word).view.mapValues(_.length).)

  val map = text.split("\\s")
    .map(it => it.toLowerCase())
    .foldLeft(Map.empty[String, Int])((acc, word) => {
      val newCount = acc.getOrElse(word, 0) + 1
      acc + (word -> newCount)
  })
  println(map)

  val xValues = 1 to 4
  val yValues = 1 to 6
  val coordinates = for {
    x <- xValues
    y <- yValues
  } yield (x, y)
  println(coordinates(20))




  def makeLazyList(v: Int): LazyList[Int] = cons(v, makeLazyList(v + 1))
  val a = makeLazyList(3)
  println(a)
  println(((a drop 66) take 3).toList)

  val list = List(87, 44, 5, 4, 200, 10, 39, 100)
  println(list.takeWhile(_ < 100))
  println(list.dropWhile(_ < 100))

  val oddAndSmallPartial: PartialFunction[Int, String] = {
    case x: Int if x % 2 != 0 && x < 100 => "Odd and less than 100"
  }
  val evenAndSmallPartial: PartialFunction[Int, String] = {
    case x: Int if x != 0 && x % 2 == 0 && x < 100 => "Even and less than 100"
  }
  val negativePartial: PartialFunction[Int, String] = {
    case x: Int if x < 0 => "Negative Number"
  }
  val largePartial: PartialFunction[Int, String] = {
    case x: Int if x > 99 => "Large Number"
  }
  val zeroPartial: PartialFunction[Int, String] = {
    case x: Int if x == 0 => "Zero"
  }
  val groupByResult:Map[String, List[Int]] = list groupBy {
    oddAndSmallPartial orElse
      evenAndSmallPartial orElse
      negativePartial orElse
      largePartial orElse
      zeroPartial
  }
  println(groupByResult)

  def doSomething(x: Int, str: String) :String = {
    s"$x - $str"
  }
  println(doSomething(1, "a"))
}
