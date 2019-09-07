package com.evolutiongaming.concurrent

import scala.collection.{BuildFrom, immutable}
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

    @deprecated("use `foldUnit1` instead", "1.0.5")
    def foldUnit[T](iter: Iterable[Future[T]]): Future[Unit] = {
      self.foldUnit1(iter)(CurrentThreadExecutionContext)
    }

    def foldUnit1[T](iter: Iterable[Future[T]])(implicit executor: ExecutionContext): Future[Unit] = {
      Future.foldLeft(iter.toList)(()) { (_, _) => () }
    }

    def foldLeft[T, S](iter: immutable.Iterable[Future[T]])(s: S)(f: (S, T) => S)(implicit ec: ExecutionContext): Future[S] = {
      val iterator = iter.iterator

      def foldLeft(s: S): Future[S] = {
        if (iterator.isEmpty) s.future
        else iterator.next().flatMap { value => foldLeft(f(s, value)) }
      }

      foldLeft(s)
    }

    def sequenceSuccessful[A, M[X] <: IterableOnce[X]](in: M[Future[A]])(implicit cbf: BuildFrom[M[Future[A]], A, M[A]], executor: ExecutionContext): Future[M[A]] = {
      in.iterator.foldLeft(Future.successful(cbf.newBuilder(in))) {
        (acc, f) => acc.flatMap(acc => f.map(acc += _).recover { case _ => acc })
      }.map(_.result())(CurrentThreadExecutionContext)
    }
  }


  implicit class FutureOps[T](val self: Future[T]) extends AnyVal {

    @deprecated("use `as` instead", "1.0.4")
    def mapVal[TT](value: TT): Future[TT] = as(value)

    def as[TT](value: TT): Future[TT] = self.map(_ => value)(CurrentThreadExecutionContext)

    def unit: Future[Unit] = as(())

    def flatten[TT](implicit ev: T <:< Future[TT]): Future[TT] = self.flatMap(ev)(CurrentThreadExecutionContext)

    def transform[TT](f: Try[T] => Try[TT])(implicit ec: ExecutionContext): Future[TT] = {
      val p = Promise[TT]()
      self.onComplete { result => p.complete(try f(result) catch { case NonFatal(t) => Failure(t) }) }
      p.future
    }
  }


  implicit class AnyFutureOps[T](val self: T) extends AnyVal {

    def future: Future[T] = Future.successful(self)

    def traverseSequentially[A, B, M[X] <: IterableOnce[X]](in: M[A])(f: A => Future[B])
      (implicit buildFrom: BuildFrom[M[A], B, M[B]]): Future[M[B]] = {

      implicit val ec = CurrentThreadExecutionContext

      val builder = buildFrom.newBuilder(in)
      builder sizeHint in.iterator.size

      in.iterator.foldLeft(Future successful builder) { (prev, next) =>
        for {
          prev <- prev
          next <- f(next)
        } yield prev += next
      } map { builder => builder.result }
    }
  }
}