package exercises

import cats.effect._
import fs2._
import java.io._

object Resources extends IOApp.Simple {

  def acquire(fileName: String) =
    IO.blocking(new BufferedReader(new FileReader(fileName)))
  def release(br: BufferedReader) = IO.blocking(br.close())
  val resource = Resource.make(acquire("sets.csv"))(release)

  def readLines(br: BufferedReader): Stream[IO, String] =
    Stream
      .repeatEval(IO.blocking(br.readLine()))
      .takeWhile(_ != null)

  def run: IO[Unit] = {
    Stream
      //.bracket(acquire("sets.csv"))(release)
      //.resource(resource)
      .fromAutoCloseable(acquire("sets.csv"))
      .flatMap(readLines)
      .take(10)
      .printlns
      .compile
      .drain
  }
}
