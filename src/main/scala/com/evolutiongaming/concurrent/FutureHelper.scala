package com.evolutiongaming.concurrent

import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.language.higherKinds

object FutureHelper {
  private val futureUnit = ().future
  private val futureNone = Option.empty.future
  private val futureSeq = Seq.empty.future
  private val futureNil = Nil.future
  private val futureTrue = true.future
  private val futureFalse = false.future


  implicit class FutureObjOps(val self: Future.type) extends AnyVal {
    def unit: Future[Unit] = futureUnit
    def none[T]: Future[Option[T]] = futureNone
    def seq[T]: Future[Seq[T]] = futureSeq
    def nil[T]: Future[List[T]] = futureNil
    def `true`: Future[Boolean] = futureTrue
    def `false`: Future[Boolean] = futureFalse
  }


  implicit class FutureOps[T](val self: Future[T]) extends AnyVal {
    def unit: Future[Unit] = self.map { _ => {} }(CurrentThreadExecutionContext)
  }


  implicit class AnyFutureOps[T](val self: T) extends AnyVal {

    def future: Future[T] = Future.successful(self)

    def traverseSequentially[A, B, M[X] <: TraversableOnce[X]](in: M[A])(f: A => Future[B])
      (implicit cbf: CanBuildFrom[M[A], B, M[B]]): Future[M[B]] = {

      implicit val ec = CurrentThreadExecutionContext

      val builder = cbf()
      builder sizeHint in.size

      in.foldLeft(Future successful builder) { (prev, next) =>
        for {
          prev <- prev
          next <- f(next)
        } yield prev += next
      } map { builder => builder.result }
    }
  }
}