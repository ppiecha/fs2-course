package exercises

import fs2._
import cats.effect._
import scala.concurrent.duration._

object EvalEvery extends IOApp.Simple {
  def evalEvery[A](d: FiniteDuration)(fa: IO[A]): Stream[IO, A] = (Stream.sleep_[IO](d) ++ Stream.eval(fa)).repeat
  def run: IO[Unit] = {
    evalEvery(1.second)(IO.println("waiting") >> IO.pure(42)).take(10).compile.toList.flatMap(IO.println)
  }
}
