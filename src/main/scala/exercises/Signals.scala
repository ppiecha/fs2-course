package exercises

import fs2._
import cats.effect._
import fs2.concurrent.SignallingRef

import scala.concurrent.duration._
import scala.util.Random

object Signals extends IOApp.Simple {

  type Temperature = Double

  def createTemperatureSensor(alarm: SignallingRef[IO, Temperature], threshold: Temperature): Stream[IO, Nothing] =
    Stream
      .repeatEval(IO(Random.between(-40.0, 40.0)))
      .evalTap(t => IO.println(f"Current temperature $t%.1f"))
      .evalMap(t => if (t > threshold) alarm.set(t) else IO.unit)
      .metered(300.millis)
      .drain

  def createCooler(alarm: SignallingRef[IO, Temperature]): Stream[IO, Nothing] =
    alarm
      .discrete
      .evalMap(t => IO.println(f"$t%.1f C is too hot. Cooling down..."))
      .drain


  val threshold = 20.0
  val initialTemperature = 20.0

  val program =
    Stream
      .eval(SignallingRef[IO].of(initialTemperature))
      .flatMap(alarm => createTemperatureSensor(alarm, threshold).concurrently(createCooler(alarm)))
      .interruptAfter(5.seconds)
      .compile
      .drain


  override def run: IO[Unit] = program
}
