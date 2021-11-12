package com.rockthejvm.udemy.advanced.part3

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Random, Success}

object Futures extends App {
  val aFuture = Future {
    Thread.sleep(1000)
    42
  }

  println(aFuture.value) //Option[Try[Iny]]
  println("waiting")
  aFuture.onComplete {
    case Success(value) => println("result is " + value)
    case Failure(ex) => println("i failed" + ex.getMessage)
  }
  Thread.sleep(3000)

  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile) = {
      println(s"${this.name} poking ${anotherProfile.name}")
    }
  }

  object SocialNetwork {
    val random = new Random()
    val names = Map(
      "fb.id.1-zuck" -> "Mark",
      "fb.id.1-bill" -> "Bill",
      "fb.id.1-dum" -> "Dummy",
    )

    val friends = Map(
      "fb.id.1-zuck" -> "fb.id.1-bill"
    )

    def fetchProfile(id: String): Future[Profile] =
      Future {
        Thread.sleep(random.nextInt(300))
        Profile(id, names(id))
      }

    def fetchBestFriend(profile: Profile): Future[Profile] =
      Future {
        Thread.sleep(random.nextInt(400))
        val friendId = friends(profile.id)
        Profile(friendId, names(friendId))
      }
  }

  val mark = SocialNetwork.fetchProfile("fb.id.1-zuck")
  //  mark.onComplete {
  //    case Success(markProfile) =>
  //      val bill = SocialNetwork.fetchBestFriend(markProfile)
  //      bill.onComplete{
  //        case Success(billProfile) => markProfile.poke(billProfile)
  //        case Failure(e) => e.printStackTrace()
  //      }
  //    case Failure(e) => e.printStackTrace()
  //  }

  //  Thread.sleep(3000)

  val name = mark.map(markProfile => markProfile.name)
  val marksBestFriend = mark.flatMap(markProfile => SocialNetwork.fetchBestFriend(markProfile))
  marksBestFriend.filter(profile => profile.name.startsWith("Z"))

  for {
    mark <- SocialNetwork.fetchProfile("fb.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } mark.poke(bill)
  Thread.sleep(3000)


  val somePRofile = SocialNetwork.fetchProfile("wrong").recover {
    case e: Throwable => Profile("dommy", "Medved")
  }
  val aFetchedProfile = SocialNetwork.fetchProfile("wrong").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.1-zuck")
  }
  val fallbackResult = SocialNetwork.fetchProfile("wrong")
    .fallbackTo(SocialNetwork.fetchProfile("fb.id.1-zuck"))


  case class User(name: String)

  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "MyBank"

    def fetchUser(name: String): Future[User] = Future {
      Thread.sleep(300)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      Thread.sleep(500)
      Transaction(user.name, merchantName, amount, "success")
    }

    def purchase(username: String, item: String, merchantName: String, cost: Double): String = {
      val trStatusFuture: Future[String] = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      Await.result(trStatusFuture, 2.seconds)
    }
  }

  println(BankingApp.purchase("medved", "shoe", "zara", 14.5))
}
