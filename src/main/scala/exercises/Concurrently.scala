package exercises

import cats.effect._
import fs2._

import scala.concurrent.duration.DurationInt

object Concurrently extends IOApp.Simple{

  val itemsToProcess = 32

  def processor(itemsProcessed: Ref[IO, Int]) =
    Stream.repeatEval(itemsProcessed.update(_ + 1))
      .take(itemsToProcess)
      .metered(100.millis)
      .drain

  def progressTracker(itemsProcessed: Ref[IO, Int]) =
    Stream.repeatEval(itemsProcessed.get.flatMap(i => IO.println(s"Progress ${i * 100 / itemsToProcess} %")))
      .metered(100.millis)
      .drain

  val refStream = Stream.eval(Ref.of[IO, Int](0)).flatMap(ref => processor(ref).concurrently(progressTracker(ref))).compile.drain

  override def run: IO[Unit] = refStream
}
