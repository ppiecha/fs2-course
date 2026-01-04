package exercises

import fs2._
import cats.effect._
import scala.concurrent.duration._

object FixedRate extends IOApp.Simple {

  def metered[A](s: Stream[IO, A], d: FiniteDuration): Stream[IO, A] =
    Stream.fixedRate[IO](d).zipRight(s)

  override def run: IO[Unit] = {
    val s = Stream
      .iterate(1)(_ + 1)
      .covary[IO]
      .evalMap(i => IO.println(s"Item $i").as(i))
      .take(10)
    metered(s, 1.second).compile.toList.flatMap(IO.println)
  }
}
