package com.rockthejvm.udemy

object CatsIntro extends App {

  import cats.Eq
  import cats.instances.int._

  val intEquality = Eq[Int]
  val aTypeSafeComparison = intEquality.eqv(1, 2)
  //use extension methods if applicable

  import cats.syntax.eq._

  val compare = 1 === 3

  //extending TC operations to composite types
  import cats.instances.list._ //we bring Eq[List[Int]]
  val aListComparison = List(1) === List(2)

  case class ToyCar(model: String, price: Double)

  implicit val toyCarEq: Eq[ToyCar] = Eq.instance[ToyCar]((car1, car2) => car1.price === car2.price)
  val compareTwoCars = ToyCar("a", 1) === ToyCar("b", 1)

  //--------------------------------------------------------------------------------------------------
  //FUNCTORS
  import cats.Functor
  import cats.Monad

  def do10xWithFunctor[F[_]](container: F[Int])(implicit functor: Functor[F]): F[Int] =
    functor.map(container)(x => x * 10)

  println(do10xWithFunctor(List(1,2,3)))
  println(do10xWithFunctor(Option(12)))

 import cats.syntax.flatMap._
 import cats.syntax.functor._
  def getPairsMonads[M[_], A, B](ma: M[A], mb: M[B])(implicit monad: Monad[M]): M[(A,B)] =
    for {
      a <- ma
      b <- mb
    } yield (a,b)

  println(getPairsMonads(List(1,2,3), List('a','b','c')))
  println(getPairsMonads(Option(42), Option('x')))
  //--------------------------------------------------------------------------------------------------
  //Applicatives = Functor + pure

  import cats.Applicative
  import cats.instances.option._

  val listAppl = Applicative[List]
  val aList = listAppl.pure(2) //List(2)

  val optionAppl = Applicative[Option]
  val anOpt = optionAppl.pure(3) //Some(2)

  import cats.syntax.applicative._
  val aSweetList = 2.pure[List]
  val aSweetOption = 2.pure[Option]

  import cats.data.Validated
  type ErrorsOr[T] = Validated[List[String], T]
  val aValidVal: ErrorsOr[Int] = Validated.valid(43)
  val aModifiedValidated = aValidVal.map(_ + 1)
  val validatedApplicative = Applicative[ErrorsOr]

//  def ap[W[_], B, T](wf: W[B => T])(wa: W[B]): W[T] = ???
  def productWithApplicatives[W[_], A, B](wa: W[A], wb: W[B])(implicit applicative: Applicative[W]): W[(A, B)] = {
    val funcWrap: W[B => (A, B)] = applicative.map(wa)(a => (b: B) => (a,b))
    applicative.ap(funcWrap)(wb)
  }

  println(productWithApplicatives(List(1,2,3), List('a', 'b')))
}
