package com.evolutiongaming.concurrent

import scala.collection.generic.CanBuildFrom
import scala.collection.immutable
import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.higherKinds
import scala.util.control.NonFatal
import scala.util.{Failure, Try}

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

    def foldUnit[T](iter: Iterable[Future[T]])(implicit ec: ExecutionContext): Future[Unit] = {
      Future.foldLeft(iter.toList)(()) { (_, _) => () }
    }

    def foldLeft[T, S](iter: immutable.Iterable[Future[T]])(s: S)(f: (S, T) => S)(implicit executor: ExecutionContext): Future[S] = {
      val iterator = iter.iterator

      def foldLeft(s: S)(implicit executor: ExecutionContext): Future[S] = {
        if (iterator.isEmpty) s.future
        else iterator.next().flatMap { value => foldLeft(f(s, value)) }
      }

      foldLeft(s)
    }

    def sequenceSuccessful[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]], executor: ExecutionContext): Future[M[A]] = {
      def wrap(f: Future[A]): Future[Option[A]] = f.map(Some.apply).recover { case _ => None }

      in.foldLeft(Future.successful(cbf(in))) {
        (acc, f) => acc.zipWith(wrap(f))(_ ++= _)
      }.map(_.result())(CurrentThreadExecutionContext)
    }
  }


  implicit class FutureOps[T](val self: Future[T]) extends AnyVal {

    def mapVal[TT](value: TT): Future[TT] = self.map(_ => value)(CurrentThreadExecutionContext)

    def unit: Future[Unit] = mapVal(())

    def flatten[TT](implicit ev: T <:< Future[TT]): Future[TT] = self.flatMap(ev)(CurrentThreadExecutionContext)

    def transform[TT](f: Try[T] => Try[TT])(implicit executor: ExecutionContext): Future[TT] = {
      val p = Promise[TT]()
      self.onComplete { result => p.complete(try f(result) catch { case NonFatal(t) => Failure(t) }) }
      p.future
    }
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