package com.evolutiongaming.concurrent

import com.evolutiongaming.concurrent.FutureHelper._
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise, TimeoutException}

class FutureHelperSpec extends FunSuite with Matchers {

  test("traverseSequentially") {

    val promise = Promise[Int]()

    val futures = List(Future.successful(1), promise.future, Future.successful(3))
    val future = Future.traverseSequentially(futures)(identity)
    the[TimeoutException] thrownBy Await.result(future, 100.millis)

    promise.success(2)

    Await.result(future, 3.seconds) shouldEqual List(1, 2, 3)
  }

  test("mapTo") {
    val future = Future.successful(1).mapVal("1")
    Await.result(future, 3.seconds) shouldEqual "1"
  }

  test("sequenceSuccesful") {
    implicit val ec = CurrentThreadExecutionContext
    val futures = List(Future.successful(1), Future.failed(new RuntimeException()), Future.successful(3))
    Await.result(Future.sequenceSuccesful(futures), 3.seconds) shouldEqual List(1, 3)
  }
}
