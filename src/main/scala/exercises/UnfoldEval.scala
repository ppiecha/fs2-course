package exercises

import fs2._
import cats.effect._

object UnfoldEval extends IOApp.Simple {
  val data = List.range(1, 100)
  val pageSize = 20

  def fetchPage(pageNumber: Int): IO[List[Int]] = {
    val start = pageNumber * pageSize
    val end = start + pageSize
    IO.println(s"Fetching page $pageNumber").as(data.slice(start, end))
  }

  def fetchAll(): Stream[IO, Int] = Stream.unfoldEval(0) { pageNumber =>
    fetchPage(pageNumber).map(elems => if (elems.isEmpty) None else Some(Stream.emits(elems), pageNumber + 1))
  }.flatten

  def run: cats.effect.IO[Unit] = fetchAll().compile.toList.flatMap(IO.println)
}
