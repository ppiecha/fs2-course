package exercises

import cats.syntax.all._
import cats.effect._
import cats.effect.std.Queue
import fs2._

import scala.concurrent.duration._

object Join extends IOApp.Simple {

  def producer(id: Int, queue: Queue[IO, Int]): Stream[IO, Nothing] =
    Stream.repeatEval(queue.offer(id)).drain

  def consumer(id: Int, queue: Queue[IO, Int]): Stream[IO, Nothing] =
    Stream.repeatEval(queue.take).map(i => s"Getting $i from consumer $id").printlns

  val queue = Queue.unbounded[IO, Int]

  val producers = (1 to 5).toList.traverse(id => queue.map(q => producer(id, q)))
  val consumers = (1 to 10).toList.traverse(id => queue.map(q => consumer(id, q)))

  def runPar(streams: Stream[IO, Nothing]*) = Stream(streams: _*).parJoinUnbounded

  override def run: IO[Unit] = {
//    producers.flatMap(ps => consumers.map(cs => {
//      val all = ps ++ cs
//      runPar(all: _*).interruptAfter(3.seconds).compile.drain
//    }))
    Stream.eval(Queue.unbounded[IO, Int]).flatMap { queue =>
      val ps = Stream.range(1, 5).map(id => producer(id, queue))
      val cs = Stream.range(1, 10).map(id => consumer(id, queue))
      val all = ps ++ cs
      all.parJoinUnbounded
    }.interruptAfter(5.seconds).compile.drain
  }
}
