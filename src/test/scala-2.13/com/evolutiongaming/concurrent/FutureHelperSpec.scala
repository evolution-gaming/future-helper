package com.evolutiongaming.concurrent

import com.evolutiongaming.concurrent.FutureHelper._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise, TimeoutException}
import scala.util.Success
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class FutureHelperSpec extends AnyFunSuite with Matchers {
  import FutureHelperSpec._

  test("traverseSequentially") {

    val promise = Promise[Int]()

    val futures = List(Future.successful(1), promise.future, Future.successful(3))
    val future = Future.traverseSequentially(futures)(identity)
    the[TimeoutException] thrownBy future.await(100.millis)

    promise.success(2)

    Await.result(future, 3.seconds) shouldEqual List(1, 2, 3)
  }

  test("sequenceSuccessful") {
    implicit val ec = CurrentThreadExecutionContext
    val futures = List(Future.successful(1), Future.failed(new RuntimeException()), Future.successful(3))
    Future.sequenceSuccessful(futures).await() shouldEqual List(1, 3)
  }

  test("as") {
    Future.successful(()).as("").await() shouldEqual ""
  }

  test("true") {
    Future.`true`.await() shouldEqual true
  }

  test("false") {
    Future.`false`.await() shouldEqual false
  }

  test("nil") {
    Future.nil[Int].await() shouldEqual Nil
  }

  test("none") {
    Future.none[Int].await() shouldEqual None
  }

  test("unit") {
    Future.unit.value shouldEqual Some(Success(()))
    Future.nil.unit.value shouldEqual Some(Success(()))
  }

  test("foldLeft") {
    val futures = List(Future.successful(1), Future.successful(2))
    val ops = FutureHelper.FutureObjOps(Future)
    ops.foldLeft(futures)(List.empty[Int]) { (s, a) => a :: s }(CurrentThreadExecutionContext).await() shouldEqual List(2, 1)
  }
}

object FutureHelperSpec {

  implicit class FutureHelperSpecFutureOps[A](val future: Future[A]) extends AnyVal {
    def await(timeout: FiniteDuration = 3.seconds): A = Await.result(future, timeout)
  }
}
