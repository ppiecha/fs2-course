package exercises

import cats.effect._
import fs2._

object FlatAttempt extends IOApp.Simple {

  implicit class RichStream[A](s: Stream[IO, A]) {
    def flatAttempt: Stream[IO, A] = {
      s.attempt.flatMap {
        case Left(e)  => Stream.empty
        case Right(a) => Stream(a)
      }
    }
  }

  def run: IO[Unit] = {
    val s = Stream.range(1, 11)
      .map(_ => math.random())
      .covary[IO]
      .map(d => {
        println(s"output $d")
        if (d < 0.8) d
        else throw new Exception("boom")
      })
    s.flatAttempt.compile.toVector.flatMap(IO.println)
  }

}
