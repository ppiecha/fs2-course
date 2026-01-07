package exercises

import cats.effect._
import cats.effect.std.Queue
import fs2._

import java.time.LocalDateTime
import scala.concurrent.duration._
import scala.util.Random

object Queues extends IOApp.Simple {

  trait Controller {
    def postAccount(
        customerId: Long,
        accountType: String,
        creationDate: LocalDateTime
    ): IO[Unit]
  }

  class Server(controller: Controller) {
    def start(): IO[Nothing] = {
      val prog = for {
        randomWait <- IO(Random.nextInt % 500)
        _ <- IO.sleep(randomWait.millis)
        _ <- controller.postAccount(
          customerId = Random.between(1L, 1000L),
          accountType = if (Random.nextBoolean()) "ira" else "brokerage",
          creationDate = LocalDateTime.now()
        )
      } yield ()
      prog.foreverM
    }
  }

  object PrintController extends Controller {
    override def postAccount(
        customerId: Long,
        accountType: String,
        creationDate: LocalDateTime
    ): IO[Unit] = IO.println(s"Initiating accunt creation. Customer: $customerId account type $accountType creation date $creationDate")
  }

  case class CreateAccountData(customerId: Long, accountType : String, creationDate: LocalDateTime)

  class QueueController(queue: Queue[IO, CreateAccountData]) extends Controller {
    override def postAccount(customerId: Long, accountType: String, creationDate: LocalDateTime): IO[Unit] =
      queue.offer(CreateAccountData(customerId, accountType, creationDate))
  }

  val program = Stream.eval(Queue.unbounded[IO, CreateAccountData]).flatMap{ queue =>
    val server = Stream.eval(new Server(new QueueController(queue)).start())
    val consumer = Stream.fromQueueUnterminated(queue).evalMap(data => IO.println(s"Getting from queue ${data.customerId} ${data.accountType}"))
    server.concurrently(consumer).interruptAfter(3.seconds)
  }


  override def run: IO[Unit] = program.compile.drain
}
