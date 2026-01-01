import fs2._
import cats.effect._

'a'.toInt
'z'.toInt
97.toChar
122.toChar
123.toChar

def lettersIter: Stream[Pure, Char] =
  Stream.iterate('a')(c => (c + 1).toChar).take(26)

lettersIter.toList

def lettersUnfold: Stream[Pure, Char] = Stream.unfold('a')(s => if (s == 'z' + 1) None else Some((s, (s + 1).toChar)))

lettersUnfold.toList

def myIterate[A](initial: A)(next: A => A) = Stream.unfold(initial)(s => Some(s, next(s)))

myIterate('a')(c => (c + 1).toChar).take(26).toList

val s1 = Stream(1, 2, 3)

s1.fold(0){case (res, i) => res + i}.toList

val nats = Stream.iterate(1)(_ + 1)

nats.take(10).toList

val odds = nats.map(i => 2 * i - 1)

odds.take(10).toList

def repeat[A](stream: Stream[Pure, A]): Stream[Pure, A] = stream ++ repeat(stream)

repeat(Stream(1, 2, 3)).take(10).toList

def unNone[A] (s: Stream[Pure, Option[A]]): Stream[Pure, A] = s.flatMap {
  case None => Stream.empty
  case Some(a) => Stream(a)
}

unNone(Stream(Some(1), None, Some(3))).take(5).toList

6/5