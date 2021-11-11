package com.rockthejvm.udemy.advanced.part1

object DarkSugars extends App {

  // #1. Single args methods
  def singleParamFunc(i: Int): String = s"arg is $i"

  val description = singleParamFunc {
    42 //write complex code
  }

  List(1, 2, 3).map { x =>
    x + 1
  }

  //#2. single abstract method
  trait Action {
    def act(x: Int): Int
  }

  val anInstance: Action = { x =>
    x + 1
  }
  val anInstance2: Action = (x:Int) => x + 1

  //#3. the :: and #:: methods are special
  val prependedList = 2 :: List(1,2,3)
  //not 2.::(List(1,2,3)) but List(1,2,3).::(2)

  //#4. multi-word method naming
  //def `and he said'() ={}


  //#5. infix types
  class Composite[A, B]
  val composite: Int Composite String = ???


  //#6. update() is very special, like apply()
  val arr = Array(1,2,3)
  arr(2) = 8// arr.update(idx, val) => arr.update(2,8)

  //#7. setters
  class Mutable {
    private var num: Int = 0

    def member = num
    def member_=(value: Int): Unit = {
      num = value
    }
  }

  val mutable = new Mutable()
  mutable.member = 42
}
