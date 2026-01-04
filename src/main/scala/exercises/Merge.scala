package exercises

import cats.effect._
import fs2._

import scala.concurrent.duration.DurationInt
import scala.util.Random

object Merge extends IOApp.Simple {
  def fetchRandomQuoteFromSource1: IO[String] = IO(Random.nextString(5))
  def fetchRandomQuoteFromSource2: IO[String] = IO(Random.nextString(25))

  val s1 = Stream.repeatEval(fetchRandomQuoteFromSource1).take(100)
  val s2 = Stream.repeatEval(fetchRandomQuoteFromSource2).take(150)
  val s3 = s1.merge(s2).interruptAfter(5.seconds)

  override def run: IO[Unit] = s3.printlns.compile.drain
}
