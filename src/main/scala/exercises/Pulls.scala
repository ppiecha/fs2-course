package exercises

import cats.effect._
import fs2._

object Pulls extends IOApp.Simple {

  def skipLimit[A](skip: Int, limit: Int)(s: Stream[IO, A]): Stream[IO, A] =
    s.pull
      .drop(skip)
      .flatMap {
        case Some(s) => s.pull.take(limit)
        case None    => Pull.done
      }
      .map(_ => ())
      .stream

  def filter[A](p: A => Boolean): Pipe[Pure, A, A] = s => {
    def go(s: Stream[Pure, A]): Pull[Pure, A, Unit] =
      s.pull.uncons.flatMap {
        case Some((chunk, restOfStream)) =>
          Pull.output(chunk.filter(p)) >> go(restOfStream)
        case None => Pull.done
      }
    go(s).stream
  }

  def runningMax: Pipe[Pure, Int, Int] = s => {
    s.scanChunksOpt(Integer.MIN_VALUE)(runningMax =>
      Some(chunk => {
        val newMax = chunk.foldLeft(Integer.MIN_VALUE)(_ max _) max runningMax
        (newMax, Chunk.singleton(newMax))
      })
    )
  }

  def run: IO[Unit] = {
    val s = Stream.iterateEval(1)(i => IO(i + 1))
    skipLimit(3, 3)(s).compile.toList.flatMap(IO.println)
  }

}
