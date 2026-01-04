package exercises

import cats.effect._
import fs2._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.util.Random

object FixedDelay extends IOApp.Simple {

  def spaced[A](s: Stream[IO, A], d: FiniteDuration): Stream[IO, A] =
    Stream.fixedDelay[IO](d).zipRight(s)

  override def run: IO[Unit] = {
    val formatter = DateTimeFormatter.ofPattern("hh:mm:ss")
    val s = Stream
      .repeatEval(
        IO.sleep(1.second) *> IO(Random.between(1, 100))
          .flatTap(_ => IO.println(LocalDateTime.now().format(formatter)))
      )
      .take(10)
    spaced(s, 1.second).compile.toList.flatMap(IO.println)
  }
}
