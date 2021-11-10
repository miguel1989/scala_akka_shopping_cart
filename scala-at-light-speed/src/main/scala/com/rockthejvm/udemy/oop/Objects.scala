package com.rockthejvm.udemy.oop

object Objects {

  abstract class MyList(num: Int) {
//    val list: List[Int] = List()
    val arr: Array[Int] = Array(num)

    def head(): Int = {
      arr.head
    }
  }
}
