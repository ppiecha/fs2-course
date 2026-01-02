package exercises

import cats.effect._
import fs2._
import scala.util.Random
import scala.concurrent.duration._

object Retry extends IOApp.Simple {

  def doEffectFailing[A](io: IO[A]): IO[A] =
    IO(math.random()).flatMap{ flag =>
      if (flag < 0.5) IO.println("failing") *> IO.raiseError(new Exception("boom"))
      else IO.println("success") *> io
    }
  
  val searches = Stream.iterateEval("")(s => IO(Random.nextPrintableChar()).map(s + _))
  def performSearch(text: String): IO[Unit] = doEffectFailing(IO.println(s"Performing search for text $text"))

  def performSearchRetrying(text: String): Stream[IO, Unit] = 
    Stream
      .retry(performSearch(text), 1.second, _.plus(1.second), 5)

  def performeSearches() =
    searches
      .metered(200.millis)
      .debounce(500.millis)
      .flatMap(performSearchRetrying)
      //.printlns
      .interruptAfter(5.seconds)

  def run: IO[Unit] = {
    //performSearchRetrying("test").compile.toVector.flatMap(IO.println)
    performeSearches()
      .compile
      .drain
  }
}
