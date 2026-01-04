package exercises

import cats.effect._
import fs2._
import cats.syntax.all._

import scala.concurrent.duration.DurationInt

object ParEvalMap extends IOApp.Simple {

  case class Job(id: Long)

  case class Event(jobId: Long, seqNo: Long)

  def processJobS(job: Job): IO[List[Event]] = {
    IO.println(s"Processing job id ${job.id}") *>
      IO.sleep(1.second) *>
      IO.pure(List.range(1, 10).map(seqNo => Event(job.id, seqNo)))
  }

  implicit class RichStream[A](s: Stream[IO, A]) {
    def parEvalMapSeq[B](maxConcurrent: Int)(f: A => IO[List[B]]): Stream[IO, B] =
      s.parEvalMap(maxConcurrent)(f).flatMap(Stream.emits)
    def parEvalMapSeqUnbounded[B](f: A => IO[List[B]]): Stream[IO, B] = parEvalMapSeq(Int.MaxValue)(f)


  }

  override def run: IO[Unit] = ???
}
