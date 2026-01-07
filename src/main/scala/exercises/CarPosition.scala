package exercises

import fs2._
import cats.effect._
import fs2.concurrent.Topic

import scala.concurrent.duration._
import scala.util.Random

object CarPosition extends IOApp.Simple {

  case class CarPosition(carId: Long, lat: Double, lng: Double)

  def createCar(
      carId: Long,
      topic: Topic[IO, CarPosition]
  ): Stream[IO, Nothing] =
    Stream
      .repeatEval(
        IO(
          CarPosition(
            carId = carId,
            lat = Random.between(-90.0, 90.0),
            lng = Random.between(-180.0, 180.0)
          )
        )
      )
      .metered(1.second)
      .through(topic.publish)

  def createGoogleUpdater(topic: Topic[IO, CarPosition]): Stream[IO, Nothing] =
    topic
      .subscribe(10)
      .evalMap(pos =>
        IO.println(
          f"Drawing position (${pos.lat}%.2f, ${pos.lng}%.2f) for car ${pos.carId} in map..."
        )
      )
      .drain

  def createDriverNotifier(
      topic: Topic[IO, CarPosition],
      shouldNotify: CarPosition => Boolean,
      notify: CarPosition => IO[Unit]
  ): Stream[IO, Nothing] =
    topic
      .subscribe(10)
      .evalMap(pos => if (shouldNotify(pos)) notify(pos) else IO.unit)
      .drain

  override def run: IO[Unit] =
    Stream.eval(Topic[IO, CarPosition]).flatMap { topic =>
      val cars = Stream.range(1, 10).map(id => createCar(id, topic))
      val updater = createGoogleUpdater(topic)
      val notifier = createDriverNotifier(
        topic = topic,
        shouldNotify = pos => pos.lat > 0,
        notify = pos => IO.println(s"Car ${pos.carId} notified")
      )
      (cars ++ Stream(updater, notifier)).parJoinUnbounded
    }.interruptAfter(3.seconds).compile.drain
}
